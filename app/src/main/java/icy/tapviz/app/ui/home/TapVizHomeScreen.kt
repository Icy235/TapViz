package icy.tapviz.app.ui.home

import android.util.Log
import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.SizeTransform
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.animateDpAsState
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.tween
import androidx.compose.animation.expandVertically
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.shrinkVertically
import androidx.compose.animation.slideInHorizontally
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutHorizontally
import androidx.compose.animation.slideOutVertically
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsPressedAsState
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBars
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.ui.res.stringResource
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.IconButton
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.NavigationBarItemDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.ColorFilter
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import icy.tapviz.app.R
import icy.tapviz.app.data.ContactField
import icy.tapviz.app.data.TapVizCard
import icy.tapviz.app.ui.card.CardFace
import icy.tapviz.app.ui.icons.TapVizIcon
import icy.tapviz.app.ui.motion.TapVizMotion
import icy.tapviz.app.ui.theme.LocalTapVizColors
import icy.tapviz.app.ui.theme.TapVizPalettes
import icy.tapviz.app.ui.theme.TapVizTheme
import kotlin.math.absoluteValue

private const val TAG = "TapVizHomeScreen"

/**
 * Варианты назначения нижней панели навигации на главном экране.
 */
private enum class HomeDestination(val label: String, val icon: String) {
    Cards("Карточки", "cards"),
    Qr("QR", "qr"),
    Profile("Профиль", "profile"),
}

/**
 * Главный экранTapViz с интерактивным карусельным просмотром карточек (HorizontalPager),
 * панелью быстрого взаимодействия (QR, Шаринг, Редактирование) и информацией о публикации.
 */
@Composable
fun TapVizHomeScreen(
    cards: List<TapVizCard>,
    onCreateCard: () -> Unit,
    onOpenQr: (TapVizCard) -> Unit,
    onShare: (TapVizCard) -> Unit,
    onEdit: (TapVizCard) -> Unit,
    onCopyLink: (TapVizCard) -> Unit,
    onOpenProfile: (TapVizCard) -> Unit,
    onNavigate: (String) -> Unit,
    modifier: Modifier = Modifier,
    selectedCardId: String? = null,
    readOnly: Boolean = false,
    showBottomNavigation: Boolean = true,
) {
    val colors = LocalTapVizColors.current
    val initialPage = remember(selectedCardId, cards) {
        cards.indexOfFirst { it.id == selectedCardId }.coerceAtLeast(0)
    }
    val pagerState = rememberPagerState(initialPage = initialPage) { cards.size.coerceAtLeast(1) }
    val selected = cards.getOrNull(pagerState.currentPage)

    LaunchedEffect(pagerState.currentPage) {
        cards.getOrNull(pagerState.currentPage)?.let { card ->
            Log.d(TAG, "Переключение активной визитки в карусели на index=${pagerState.currentPage}, id=${card.id}")
        }
    }

    Scaffold(
        modifier = modifier.fillMaxSize(),
        containerColor = colors.background,
        contentWindowInsets = WindowInsets.navigationBars,
        bottomBar = {
            if (showBottomNavigation) {
                HomeNavigation(
                    selected = HomeDestination.Cards,
                    onSelect = { destination ->
                        if (destination != HomeDestination.Cards) {
                            Log.d(TAG, "Навигация на раздел: ${destination.name}")
                            onNavigate(destination.name)
                        }
                    },
                )
            }
        },
    ) { scaffoldPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(scaffoldPadding)
                .verticalScroll(rememberScrollState()),
        ) {
            HomeHeader(onCreateCard = onCreateCard)
            Column(modifier = Modifier.padding(top = 14.dp)) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 22.dp),
                    verticalAlignment = Alignment.Bottom,
                ) {
                    Column(modifier = Modifier.weight(1f)) {
                        Text(stringResource(R.string.your_cards), fontSize = 31.sp, lineHeight = 36.sp, fontWeight = FontWeight.Black, letterSpacing = (-0.5).sp, color = colors.textPrimary)
                        Text(stringResource(R.string.swipe_to_share_hint), fontSize = 12.sp, fontWeight = FontWeight.Medium, color = colors.textSecondary)
                    }
                    if (cards.isNotEmpty()) AnimatedCounter(current = pagerState.currentPage + 1, total = cards.size)
                }
            }

            if (cards.isEmpty()) {
                EmptyCardsPanel(onCreateCard = onCreateCard)
            } else {
                HorizontalPager(
                    state = pagerState,
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(338.dp)
                        .padding(top = 16.dp),
                    contentPadding = PaddingValues(horizontal = 22.dp),
                    pageSpacing = 13.dp,
                    key = { index -> cards[index].id },
                ) { page ->
                    val offset = ((pagerState.currentPage - page) + pagerState.currentPageOffsetFraction).absoluteValue
                    CardFace(
                        card = cards[page],
                        active = (page == pagerState.currentPage),
                        showStatus = true,
                        modifier = Modifier
                            .clickable {
                                Log.i(TAG, "Клик по визитке id=${cards[page].id} -> переход в редактирование")
                                onEdit(cards[page])
                            }
                            .graphicsLayer {
                                val progress = offset.coerceIn(0f, 1f)
                                val depth = 1f - (progress * TapVizMotion.CardDepthScale)
                                scaleX = depth
                                scaleY = depth
                                rotationY = ((pagerState.currentPage - page) + pagerState.currentPageOffsetFraction) * TapVizMotion.CardPerspectiveDegrees
                                cameraDistance = 28f * density
                                alpha = 1f - (progress * .18f)
                                translationY = progress * TapVizMotion.CardDepthOffsetDp.dp.toPx()
                            },
                    )
                }

                PagePips(current = pagerState.currentPage, count = cards.size)

                if (selected != null) {
                    AnimatedContent(
                        targetState = pagerState.currentPage,
                        transitionSpec = {
                            val forward = targetState > initialState
                            (fadeIn(tween(TapVizMotion.CardDetailsEnterDurationMs)) +
                                slideInHorizontally(tween(TapVizMotion.CardDetailsEnterDurationMs)) { width -> if (forward) width / 3 else -width / 3 }) togetherWith
                                (fadeOut(tween(TapVizMotion.CardDetailsExitDurationMs)) +
                                    slideOutHorizontally(tween(TapVizMotion.CardDetailsExitDurationMs)) { width -> if (forward) -width / 4 else width / 4 })
                        },
                        label = "selected_card_details",
                    ) { page ->
                        cards.getOrNull(page)?.let { card ->
                            Column {
                                ActionRow(
                                    onQr = {
                                        Log.i(TAG, "Клик на открыть QR-код для визитки id=${card.id}")
                                        onOpenQr(card)
                                    },
                                    onShare = {
                                        Log.i(TAG, "Клик на поделиться визиткой id=${card.id}")
                                        onShare(card)
                                    },
                                    onEdit = {
                                        Log.i(TAG, "Клик на редактировать визитку id=${card.id}")
                                        onEdit(card)
                                    },
                                )
                                PublicationPanel(
                                    card = card,
                                    onCopy = {
                                        Log.i(TAG, "Копирование публичной ссылки id=${card.id}")
                                        onCopyLink(card)
                                    },
                                    onOpen = {
                                        Log.i(TAG, "Открытие публичной страницы id=${card.id}")
                                        onOpenProfile(card)
                                    },
                                )
                            }
                        }
                    }
                }

                if (!readOnly) {
                    Button(
                        onClick = {
                            Log.i(TAG, "Нажата кнопка 'Создать новую карточку'")
                            onCreateCard()
                        },
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 22.dp)
                            .height(54.dp),
                        shape = RoundedCornerShape(18.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = colors.accentSoft, contentColor = colors.accent),
                        contentPadding = PaddingValues(horizontal = 18.dp),
                    ) {
                        TapVizIcon("add", stringResource(R.string.action_create), colors.accent, size = 18.dp)
                        Spacer(Modifier.width(9.dp))
                        Text(stringResource(R.string.create_new_card), fontWeight = FontWeight.SemiBold, fontSize = 13.sp)
                    }
                }
                Spacer(Modifier.height(22.dp))
            }
        }
    }
}

/**
 * Верхняя шапка главного экрана с брендингом и кнопкой добавления.
 */
@Composable
private fun HomeHeader(onCreateCard: () -> Unit) {
    val colors = LocalTapVizColors.current
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(start = 20.dp, end = 16.dp, top = 14.dp, bottom = 7.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Image(
            painter = painterResource(R.drawable.tapviz_logo_main),
            contentDescription = "TapViz",
            colorFilter = ColorFilter.tint(colors.textPrimary),
            modifier = Modifier.size(width = 31.dp, height = 25.dp),
        )
        Spacer(Modifier.width(10.dp))
        Text(stringResource(R.string.app_name), fontSize = 12.sp, letterSpacing = 2.4.sp, fontWeight = FontWeight.Bold, color = colors.textPrimary)
        Spacer(Modifier.weight(1f))
        IconButton(
            onClick = onCreateCard,
            modifier = Modifier
                .size(42.dp)
                .clip(CircleShape)
                .background(colors.surfaceRaised),
        ) {
            TapVizIcon("add", stringResource(R.string.create_card), colors.accent, size = 22.dp)
        }
    }
}

/**
 * Счетчик текущей страницы карусели визиток.
 */
@Composable
private fun AnimatedCounter(current: Int, total: Int) {
    val colors = LocalTapVizColors.current
    Row(verticalAlignment = Alignment.CenterVertically) {
        AnimatedContent(
            targetState = current,
            transitionSpec = {
                (slideInVertically(animationSpec = spring(stiffness = Spring.StiffnessMediumLow)) { it / 2 } + fadeIn(tween(TapVizMotion.CounterEnterDurationMs))) togetherWith
                    (slideOutVertically(animationSpec = spring(stiffness = Spring.StiffnessMediumLow)) { -it / 2 } + fadeOut(tween(TapVizMotion.CounterExitDurationMs))) using
                    SizeTransform(clip = false)
            },
            label = "selected_card_number",
        ) { page ->
            Text(page.toString().padStart(2, '0'), fontSize = 17.sp, fontWeight = FontWeight.Bold, color = colors.accent)
        }
        Text(" / ${total.toString().padStart(2, '0')}", fontSize = 11.sp, color = colors.textMuted)
    }
}

/**
 * Точечный индикатор страниц (Page Indicator).
 */
@Composable
private fun PagePips(current: Int, count: Int) {
    val colors = LocalTapVizColors.current
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .height(31.dp),
        horizontalArrangement = Arrangement.Center,
        verticalAlignment = Alignment.CenterVertically,
    ) {
        repeat(count) { page ->
            val selected = (page == current)
            val width by animateDpAsState(
                targetValue = if (selected) 25.dp else 7.dp,
                animationSpec = spring(dampingRatio = .58f, stiffness = Spring.StiffnessMedium),
                label = "card_page_pip",
            )
            Box(
                Modifier
                    .padding(horizontal = 4.dp)
                    .size(width = width, height = 6.dp)
                    .clip(CircleShape)
                    .background(if (selected) colors.accent else colors.outline),
            )
        }
    }
}

/**
 * Ряд быстрой панели действий для карточки.
 */
@Composable
private fun ActionRow(onQr: () -> Unit, onShare: () -> Unit, onEdit: () -> Unit) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 22.dp)
            .padding(top = 2.dp, bottom = 13.dp),
        horizontalArrangement = Arrangement.spacedBy(8.dp),
    ) {
        ActionTile("qr", "QR", Modifier.weight(1f), onQr, emphasized = true)
        ActionTile("share", "Поделиться", Modifier.weight(1f), onShare)
        ActionTile("edit", "Править", Modifier.weight(1f), onEdit)
        ActionTile("nfc", "NFC", Modifier.weight(1f), {}, enabled = false)
    }
}

/**
 * Стилизованная плитка действия.
 */
@Composable
private fun ActionTile(
    icon: String,
    label: String,
    modifier: Modifier = Modifier,
    onClick: () -> Unit,
    emphasized: Boolean = false,
    enabled: Boolean = true,
) {
    val colors = LocalTapVizColors.current
    val interaction = remember { MutableInteractionSource() }
    val pressScale by animateFloatAsState(
        targetValue = if (enabled && interaction.collectIsPressedAsState().value) TapVizMotion.ActionPressScale else 1f,
        animationSpec = spring(dampingRatio = .48f, stiffness = Spring.StiffnessHigh),
        label = "action_press_scale",
    )
    Column(
        modifier = modifier
            .height(76.dp)
            .scale(pressScale)
            .clip(RoundedCornerShape(18.dp))
            .background(if (emphasized) colors.accentSoft else colors.surface)
            .run { if (enabled) clickable(interactionSource = interaction, indication = null, onClick = onClick) else this }
            .padding(horizontal = 4.dp, vertical = 9.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center,
    ) {
        val tint = if (!enabled) colors.textMuted.copy(alpha = 0.5f) else if (emphasized) colors.accent else colors.textPrimary
        TapVizIcon(icon, label, tint, size = 20.dp)
        Spacer(Modifier.height(4.dp))
        Text(
            label,
            fontSize = 11.sp,
            fontWeight = FontWeight.SemiBold,
            color = if (!enabled) colors.textMuted.copy(alpha = 0.5f) else if (emphasized) colors.accent else colors.textSecondary,
            maxLines = 1,
        )
    }
}

/**
 * Информационная карточка состояния публикации и ссылки.
 */
@Composable
private fun PublicationPanel(card: TapVizCard, onCopy: () -> Unit, onOpen: () -> Unit) {
    val colors = LocalTapVizColors.current
    var expanded by remember(card.id, card.status) { mutableStateOf(true) }

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 22.dp)
            .padding(bottom = 13.dp),
        shape = RoundedCornerShape(23.dp),
        colors = CardDefaults.cardColors(containerColor = colors.surface),
    ) {
        Column(
            Modifier
                .fillMaxWidth()
                .padding(horizontal = 17.dp, vertical = 15.dp),
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(10.dp))
                    .clickable { expanded = !expanded },
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Box(
                    Modifier
                        .size(9.dp)
                        .clip(CircleShape)
                        .background(if (card.status == "published") colors.success else colors.textMuted),
                )
                Spacer(Modifier.width(9.dp))
                AnimatedContent(
                    targetState = (card.status == "published"),
                    transitionSpec = {
                        (fadeIn(tween(180)) + slideInVertically(tween(220)) { it / 3 }) togetherWith
                            (fadeOut(tween(100)) + slideOutVertically(tween(140)) { -it / 3 })
                    },
                    label = "publication_state",
                ) { published ->
                    Text(if (published) "Опубликована" else "Черновик", fontSize = 12.sp, fontWeight = FontWeight.Bold, color = colors.textPrimary)
                }
                Spacer(Modifier.weight(1f))
                TapVizIcon(if (expanded) "up" else "down", "Развернуть", colors.textMuted, size = 16.dp)
            }
            AnimatedVisibility(
                visible = expanded,
                enter = expandVertically(spring(dampingRatio = .82f, stiffness = Spring.StiffnessMediumLow)) + fadeIn(tween(TapVizMotion.LinkRevealDurationMs)),
                exit = shrinkVertically(spring(dampingRatio = .9f, stiffness = Spring.StiffnessMedium)) + fadeOut(tween(TapVizMotion.LinkCollapseDurationMs)),
            ) {
                Column {
                    Spacer(Modifier.height(12.dp))
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(14.dp))
                            .background(colors.background)
                            .padding(start = 13.dp, end = 6.dp, top = 6.dp, bottom = 6.dp),
                        verticalAlignment = Alignment.CenterVertically,
                    ) {
                        Column(Modifier.weight(1f)) {
                            Text(stringResource(R.string.public_address_caps), fontSize = 11.sp, letterSpacing = 1.1.sp, color = colors.textMuted)
                            Spacer(Modifier.height(3.dp))
                            Text(
                                "/${card.slug}",
                                fontSize = 12.sp,
                                fontWeight = FontWeight.SemiBold,
                                color = if (card.status == "published") colors.textPrimary else colors.textMuted,
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis,
                            )
                        }
                        ActionChip(stringResource(R.string.copy), onCopy)
                    }
                    Spacer(Modifier.height(8.dp))
                    TextButtonLabel("Открыть страницу", onOpen)
                }
            }
        }
    }
}

@Composable
private fun ActionChip(label: String, onClick: () -> Unit) {
    val colors = LocalTapVizColors.current
    Column(
        modifier = Modifier
            .clip(RoundedCornerShape(11.dp))
            .clickable(onClick = onClick)
            .padding(horizontal = 9.dp, vertical = 5.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        TapVizIcon("cards", label, colors.accent, size = 19.dp)
        Text(label, fontSize = 11.sp, color = colors.textMuted)
    }
}

@Composable
private fun TextButtonLabel(label: String, onClick: () -> Unit) {
    val colors = LocalTapVizColors.current
    Row(
        modifier = Modifier
            .clip(RoundedCornerShape(8.dp))
            .clickable(onClick = onClick)
            .padding(vertical = 6.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Text(label, fontSize = 13.sp, fontWeight = FontWeight.SemiBold, color = colors.accent)
        Spacer(Modifier.width(5.dp))
        TapVizIcon("share", label, colors.accent, size = 13.dp)
    }
}

@Composable
private fun EmptyCardsPanel(onCreateCard: () -> Unit) {
    val colors = LocalTapVizColors.current
    Column(
        Modifier
            .fillMaxWidth()
            .padding(horizontal = 22.dp, vertical = 26.dp)
            .clip(RoundedCornerShape(26.dp))
            .background(colors.surface)
            .padding(24.dp),
        horizontalAlignment = Alignment.Start,
    ) {
        Text(stringResource(R.string.empty_cards_title), fontSize = 22.sp, fontWeight = FontWeight.Bold, color = colors.textPrimary)
        Spacer(Modifier.height(6.dp))
        Text(stringResource(R.string.empty_cards_message), fontSize = 12.sp, color = colors.textSecondary)
        Spacer(Modifier.height(18.dp))
        Button(
            onClick = onCreateCard,
            shape = RoundedCornerShape(15.dp),
            colors = ButtonDefaults.buttonColors(containerColor = colors.accent, contentColor = colors.accentContent),
        ) {
            TapVizIcon("add", stringResource(R.string.action_create), colors.accentContent, size = 16.dp)
            Spacer(Modifier.width(8.dp))
            Text(stringResource(R.string.create_card), fontWeight = FontWeight.Bold)
        }
    }
}

@Composable
private fun HomeNavigation(selected: HomeDestination, onSelect: (HomeDestination) -> Unit) {
    val colors = LocalTapVizColors.current
    NavigationBar(containerColor = colors.surface, tonalElevation = 0.dp) {
        HomeDestination.entries.forEach { destination ->
            NavigationBarItem(
                selected = (selected == destination),
                onClick = { onSelect(destination) },
                icon = {
                    TapVizIcon(
                        destination.icon,
                        destination.label,
                        if (selected == destination) colors.accent else colors.textMuted,
                        size = 20.dp,
                    )
                },
                label = { Text(destination.label, fontSize = 11.sp) },
                colors = NavigationBarItemDefaults.colors(
                    selectedIconColor = colors.accent,
                    selectedTextColor = colors.accent,
                    indicatorColor = colors.accentSoft,
                    unselectedIconColor = colors.textMuted,
                    unselectedTextColor = colors.textMuted,
                ),
            )
        }
    }
}

// ==================== PREVIEWS (ПРЕВЬЮ ГЛАВНОГО ЭКРАНА) ====================

@Preview(name = "TapViz · Electric cyan · Home", showBackground = true, backgroundColor = 0xFF080B10, widthDp = 393, heightDp = 852)
@Composable
private fun TapVizHomeScreenPreview() {
    TapVizTheme(darkTheme = true, palette = TapVizPalettes.ElectricCyan) {
        TapVizHomeScreen(
            cards = homePreviewCards,
            onCreateCard = {},
            onOpenQr = {},
            onShare = {},
            onEdit = {},
            onCopyLink = {},
            onOpenProfile = {},
            onNavigate = {},
        )
    }
}

@Preview(name = "TapViz · Ion violet · Home", showBackground = true, backgroundColor = 0xFF080B10, widthDp = 393, heightDp = 852)
@Composable
private fun TapVizHomeVioletPreview() {
    TapVizTheme(darkTheme = true, palette = TapVizPalettes.IonViolet) {
        TapVizHomeScreen(
            cards = homePreviewCards,
            onCreateCard = {},
            onOpenQr = {},
            onShare = {},
            onEdit = {},
            onCopyLink = {},
            onOpenProfile = {},
            onNavigate = {},
        )
    }
}

val homePreviewCards = listOf(
    TapVizCard(
        id = "home-preview-1",
        slug = "anna-petrova",
        label = "Основная",
        displayName = "Анна Петрова",
        role = "Продуктовый дизайнер",
        organization = "North Studio",
        bio = "",
        theme = "ink",
        status = "published",
        version = 3,
        fields = listOf(
            ContactField("home-phone", "phone", "Телефон", "+7 900 123-45-67", "public"),
            ContactField("home-email", "email", "Email", "anna@example.com", "public"),
            ContactField("home-site", "website", "Портфолио", "https://anna.design", "public"),
        ),
    ),
    TapVizCard(
        id = "home-preview-2",
        slug = "anna-lecture",
        label = "Лекции",
        displayName = "Анна Петрова",
        role = "Спикер · UX-дизайн",
        organization = "",
        bio = "",
        theme = "moss",
        status = "draft",
        version = 1,
        fields = listOf(ContactField("lecture-email", "email", "Почта", "anna@example.com", "public")),
    ),
    TapVizCard(
        id = "home-preview-3",
        slug = "anna-studio",
        label = "North Studio",
        displayName = "Анна Петрова",
        role = "Партнёр студии",
        organization = "North Studio",
        bio = "",
        theme = "plum",
        status = "published",
        version = 2,
        fields = emptyList(),
    ),
)
