/*
 * Kiwix Android
 * Copyright (c) 2025 Kiwix <android.kiwix.org>
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program. If not, see <http://www.gnu.org/licenses/>.
 *
 */

package org.kiwix.kiwixmobile.nav.destination.library.local

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyListState
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.BottomAppBarScrollBehavior
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.ScrollableTabRow
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Tab
import androidx.compose.material3.TabRowDefaults
import androidx.compose.material3.TabRowDefaults.tabIndicatorOffset
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.material3.TopAppBarScrollBehavior
import androidx.compose.runtime.Composable
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.semantics.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.IntOffset
import androidx.navigation.NavHostController
import kotlinx.coroutines.launch
import org.kiwix.kiwixmobile.R.drawable
import org.kiwix.kiwixmobile.R.string
import org.kiwix.kiwixmobile.core.R
import org.kiwix.kiwixmobile.core.base.BackPressActivityExtensions
import org.kiwix.kiwixmobile.core.main.reader.OnBackPressed
import org.kiwix.kiwixmobile.core.ui.components.ContentLoadingProgressBar
import org.kiwix.kiwixmobile.core.ui.components.KiwixAppBar
import org.kiwix.kiwixmobile.core.ui.components.KiwixButton
import org.kiwix.kiwixmobile.core.ui.components.KiwixFloatingActionButton
import org.kiwix.kiwixmobile.core.ui.components.KiwixSnackbarHost
import org.kiwix.kiwixmobile.core.ui.components.ProgressBarStyle
import org.kiwix.kiwixmobile.core.ui.components.SwipeRefreshLayout
import org.kiwix.kiwixmobile.core.ui.models.ActionMenuItem
import org.kiwix.kiwixmobile.core.utils.ComposeDimens.EIGHT_DP
import org.kiwix.kiwixmobile.core.utils.ComposeDimens.FOUR_DP
import org.kiwix.kiwixmobile.core.utils.ComposeDimens.SIX_DP
import org.kiwix.kiwixmobile.core.utils.ComposeDimens.TEN_DP
import org.kiwix.kiwixmobile.core.utils.ComposeDimens.TWENTY_DP
import org.kiwix.kiwixmobile.core.utils.ZERO
import org.kiwix.kiwixmobile.core.zim_manager.fileselect_view.BooksOnDiskListItem
import org.kiwix.kiwixmobile.core.zim_manager.fileselect_view.BooksOnDiskListItem.BookOnDisk
import org.kiwix.kiwixmobile.nav.destination.library.local.LocalLibraryViewModel.LocalLibraryUiState
import org.kiwix.kiwixmobile.nav.destination.library.local.LocalLibraryViewModel.NoFileView
import org.kiwix.kiwixmobile.nav.destination.library.online.DOWNLOAD_BOOK_ITEM_TESTING_TAG
import org.kiwix.kiwixmobile.nav.destination.library.online.DownloadBookItem
import org.kiwix.kiwixmobile.ui.BookItem
import org.kiwix.kiwixmobile.ui.ZimFilesLanguageHeader
import org.kiwix.kiwixmobile.zimManager.fileselectView.FileSelectListState
import org.kiwix.kiwixmobile.zimManager.libraryView.LibraryListItem.LibraryDownloadItem
import kotlin.math.roundToInt

const val NO_FILE_TEXT_TESTING_TAG = "noFileTextTestingTag"
const val DOWNLOAD_BUTTON_TESTING_TAG = "downloadButtonTestingTag"
const val BOOK_LIST_TESTING_TAG = "bookListTestingTag"
const val SELECT_FILE_BUTTON_TESTING_TAG = "selectFileButtonTestingTag"
const val LOCAL_FILE_TRANSFER_MENU_BUTTON_TESTING_TAG = "localFileTransferMenuButtonTestingTag"
const val SHOW_SWIPE_DOWN_TO_SCAN_FILE_SYSTEM_TEXT_TESTING_TAG =
  "showSwipeDownToScanFileSystemTextTestingTag"
const val LOCAL_LIBRARY_TABS_ROW_TESTING_TAG = "localLibraryTabsRowTestingTag"
const val LOCAL_LIBRARY_TAB_TESTING_TAG_PREFIX = "localLibraryTabTestingTag_"
const val LOCAL_DOWNLOAD_LIST_TESTING_TAG = "localDownloadListTestingTag"
private const val BACK_TO_TOP_ITEM_THRESHOLD = 5

@OptIn(ExperimentalMaterial3Api::class)
@Suppress("ComposableLambdaParameterNaming", "LongParameterList")
@Composable
fun LocalLibraryScreen(
  state: LocalLibraryUiState,
  actionMenuItems: List<ActionMenuItem>,
  listState: LazyListState,
  onRefresh: () -> Unit,
  onDownloadButtonClick: () -> Unit,
  onClick: ((BookOnDisk) -> Unit)? = null,
  onLongClick: ((BookOnDisk) -> Unit)? = null,
  onMultiSelect: ((BookOnDisk) -> Unit)? = null,
  onLibraryTabClick: () -> Unit = {},
  onDownloadsTabClick: () -> Unit = {},
  onPauseResumeClick: (LibraryDownloadItem) -> Unit = {},
  onStopClick: (LibraryDownloadItem) -> Unit = {},
  bottomAppBarScrollBehaviour: BottomAppBarScrollBehavior?,
  onUserBackPressed: () -> BackPressActivityExtensions.Super,
  navHostController: NavHostController,
  snackbarHostState: SnackbarHostState,
  navigationIcon: @Composable () -> Unit
) {
  val downloadsListState = rememberLazyListState()
  val activeListState = if (state.isDownloadsTabSelected) downloadsListState else listState
  val scrollBehavior = TopAppBarDefaults.enterAlwaysScrollBehavior()
  Scaffold(
    snackbarHost = { KiwixSnackbarHost(snackbarHostState = snackbarHostState) },
    topBar = {
      KiwixAppBar(
        title = screenTitle(state.fileSelectListState),
        navigationIcon = navigationIcon,
        actionMenuItems = actionMenuItems,
        topAppBarScrollBehavior = scrollBehavior
      )
    },
    floatingActionButton = {
      LocalLibraryBackToTopButton(
        listState = activeListState,
        scrollBehavior = scrollBehavior,
        bottomAppBarScrollBehaviour = bottomAppBarScrollBehaviour
      )
    },
    modifier = Modifier
      .nestedScroll(scrollBehavior.nestedScrollConnection)
      .let { baseModifier ->
        bottomAppBarScrollBehaviour?.let {
          baseModifier.nestedScroll(it.nestedScrollConnection)
        } ?: baseModifier
      }
  ) { contentPadding ->
    LocalLibraryMainContent(
      state,
      onRefresh,
      contentPadding,
      onUserBackPressed,
      navHostController,
      onDownloadButtonClick,
      onClick,
      onLongClick,
      onMultiSelect,
      onLibraryTabClick,
      onDownloadsTabClick,
      onPauseResumeClick,
      onStopClick,
      activeListState
    )
  }
}

@Composable
private fun screenTitle(fileSelectListState: FileSelectListState): String =
  if (fileSelectListState.selectedBooks.isNotEmpty()) {
    "${fileSelectListState.selectedBooks.size}"
  } else {
    stringResource(R.string.library)
  }


@Composable
private fun LocalLibraryTabsRow(
  isDownloadsSelected: Boolean,
  onLibraryTabClick: () -> Unit,
  onDownloadsTabClick: () -> Unit
) {
  Box(modifier = Modifier.fillMaxWidth()) {
    HorizontalDivider(
      modifier = Modifier
        .fillMaxWidth()
        .align(Alignment.BottomCenter),
      color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f)
    )
    ScrollableTabRow(
      selectedTabIndex = if (isDownloadsSelected) 1 else 0,
      edgePadding = SIXTEEN_DP,
      containerColor = Color.Transparent,
      divider = {},
      indicator = { tabPositions ->
        tabPositions.getOrNull(if (isDownloadsSelected) 1 else 0)?.let { position ->
          TabRowDefaults.SecondaryIndicator(
            Modifier.tabIndicatorOffset(position),
            color = MaterialTheme.colorScheme.primary
          )
        }
      },
      modifier = Modifier.semantics { testTag = LOCAL_LIBRARY_TABS_ROW_TESTING_TAG }
    ) {
      listOf(
        stringResource(R.string.library) to onLibraryTabClick,
        stringResource(R.string.downloading) to onDownloadsTabClick
      ).forEachIndexed { index, (title, onClick) ->
        val isSelected = index == if (isDownloadsSelected) 1 else 0
        Tab(
          selected = isSelected,
          onClick = onClick,
          text = {
            Text(
              text = title,
              style = MaterialTheme.typography.labelLarge,
              fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
              color = if (isSelected) {
                MaterialTheme.colorScheme.primary
              } else {
                MaterialTheme.colorScheme.onSurfaceVariant
              }
            )
          },
          modifier = Modifier
            .height(FORTY_EIGHT_DP)
            .semantics { testTag = "$LOCAL_LIBRARY_TAB_TESTING_TAG_PREFIX$index" }
        )
      }
    }
  }
}
@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun LocalLibraryMainContent(
  state: LocalLibraryUiState,
  onRefresh: () -> Unit,
  contentPadding: PaddingValues,
  onUserBackPressed: () -> BackPressActivityExtensions.Super,
  navHostController: NavHostController,
  onDownloadButtonClick: () -> Unit,
  onClick: ((BookOnDisk) -> Unit)? = null,
  onLongClick: ((BookOnDisk) -> Unit)? = null,
  onMultiSelect: ((BookOnDisk) -> Unit)? = null,
  onLibraryTabClick: () -> Unit,
  onDownloadsTabClick: () -> Unit,
  onPauseResumeClick: (LibraryDownloadItem) -> Unit,
  onStopClick: (LibraryDownloadItem) -> Unit,
  listState: LazyListState
) {
  Column(
    modifier = Modifier
      .fillMaxSize()
      .padding(contentPadding)
  ) {
    if (state.downloadItems.isNotEmpty()) {
      LocalLibraryTabsRow(
        isDownloadsSelected = state.isDownloadsTabSelected,
        onLibraryTabClick = onLibraryTabClick,
        onDownloadsTabClick = onDownloadsTabClick
      )
    }
    SwipeRefreshLayout(
      isRefreshing = state.isSwipeRefreshing,
      isEnabled = !state.scanning.isScanning,
      onRefresh = onRefresh,
      modifier = Modifier
        .fillMaxWidth()
        .weight(1f)
    ) {
      OnBackPressed(onUserBackPressed, navHostController)
      if (state.scanning.isScanning && !state.isDownloadsTabSelected) {
        ContentLoadingProgressBar(
          progressBarStyle = ProgressBarStyle.HORIZONTAL,
          progress = state.scanning.progress
        )
      }
      when {
        state.isDownloadsTabSelected -> DownloadItemsList(
          items = state.downloadItems,
          listState = listState,
          onPauseResumeClick = onPauseResumeClick,
          onStopClick = onStopClick
        )

        state.noFileView.isVisible || state.fileSelectListState.bookOnDiskListItems.isEmpty() ->
          NoFilesView(state.noFileView, onDownloadButtonClick)

        else -> BookItemList(
          state.fileSelectListState,
          onClick,
          onLongClick,
          onMultiSelect,
          listState
        )
      }
    }
  }
}

@Composable
private fun DownloadItemsList(
  items: List<LibraryDownloadItem>,
  listState: LazyListState,
  onPauseResumeClick: (LibraryDownloadItem) -> Unit,
  onStopClick: (LibraryDownloadItem) -> Unit
) {
  LazyColumn(
    modifier = Modifier
      .fillMaxSize()
      .testTag(LOCAL_DOWNLOAD_LIST_TESTING_TAG),
    state = listState
  ) {
    itemsIndexed(items, key = { _, item -> item.id }) { index, item ->
      DownloadBookItem(
        index = index,
        item = item,
        onPauseResumeClick = onPauseResumeClick,
        onStopClick = onStopClick
      )
    }
  }
}

@Composable
private fun BookItemList(
  state: FileSelectListState,
  onClick: ((BookOnDisk) -> Unit)? = null,
  onLongClick: ((BookOnDisk) -> Unit)? = null,
  onMultiSelect: ((BookOnDisk) -> Unit)? = null,
  lazyListState: LazyListState,
) {
  LazyColumn(
    modifier = Modifier
      .fillMaxSize()
      .testTag(BOOK_LIST_TESTING_TAG),
    state = lazyListState
  ) {
    itemsIndexed(state.bookOnDiskListItems) { index, bookItem ->
      when (bookItem) {
        is BooksOnDiskListItem.LanguageItem -> {
          ZimFilesLanguageHeader(bookItem)
        }

        is BookOnDisk -> {
          BookItem(
            index = index,
            bookOnDisk = bookItem,
            selectionMode = state.selectionMode,
            onClick = onClick,
            onLongClick = onLongClick,
            onMultiSelect = onMultiSelect
          )
        }
      }
    }
    item {
      Box(
        modifier = Modifier
          .fillMaxWidth()
          .padding(vertical = TEN_DP),
        contentAlignment = Alignment.Center
      ) {
        SwipeDownToScanFileSystemText()
      }
    }
  }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun LocalLibraryBackToTopButton(
  listState: LazyListState,
  scrollBehavior: TopAppBarScrollBehavior,
  bottomAppBarScrollBehaviour: BottomAppBarScrollBehavior?
) {
  val coroutineScope = rememberCoroutineScope()
  val shouldShowBackToTopButton by remember {
    derivedStateOf { listState.firstVisibleItemIndex >= BACK_TO_TOP_ITEM_THRESHOLD }
  }

  AnimatedVisibility(
    visible = shouldShowBackToTopButton,
    enter = slideInVertically { it },
    exit = slideOutVertically { it }
  ) {
    KiwixFloatingActionButton(
      icon = painterResource(id = R.drawable.ic_arrow_upward_24dp),
      onClick = {
        coroutineScope.launch {
          // Manually reset the topAppBar and bottomAppBar scroll offsets
          // so they become visible when scrolling to top programmatically.
          // animateScrollToItem alone does not update the nestedScrollConnection.
          scrollBehavior.state.heightOffset = 0f
          scrollBehavior.state.contentOffset = 0f
          bottomAppBarScrollBehaviour?.state?.heightOffset = 0f
          bottomAppBarScrollBehaviour?.state?.contentOffset = 0f
          listState.animateScrollToItem(ZERO)
        }
      },
      contentDescription = stringResource(R.string.pref_back_to_top),
      shouldPulse = true
    )
  }
}

@Composable
fun NoFilesView(
  noFilesView: NoFileView,
  onDownloadButtonClick: () -> Unit
) {
  Column(
    modifier = Modifier
      .fillMaxSize()
      .padding(horizontal = FOUR_DP)
      .verticalScroll(rememberScrollState()),
    horizontalAlignment = Alignment.CenterHorizontally,
    verticalArrangement = Arrangement.Center
  ) {
    Text(
      modifier = Modifier.testTag(NO_FILE_TEXT_TESTING_TAG),
      text = noFilesView.title,
      style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Medium),
      textAlign = TextAlign.Center
    )
    Spacer(modifier = Modifier.height(EIGHT_DP))
    KiwixButton(
      buttonText = noFilesView.buttonText,
      clickListener = onDownloadButtonClick,
      modifier = Modifier.testTag(DOWNLOAD_BUTTON_TESTING_TAG)
    )
    SwipeDownToScanFileSystemText()
  }
}

@Suppress("MagicNumber")
@Composable
private fun SwipeDownToScanFileSystemText() {
  val infiniteTransition = rememberInfiniteTransition(label = "swipeAnim")
  val density = LocalDensity.current
  val targetOffsetPx = with(density) { TWENTY_DP.toPx() }
  val offsetY by infiniteTransition.animateFloat(
    initialValue = ZERO.toFloat(),
    targetValue = targetOffsetPx,
    animationSpec = infiniteRepeatable(
      animation = tween(SWIPE_DOWN_IMAGE_ANIMATION_TIME, easing = FastOutSlowInEasing),
      repeatMode = RepeatMode.Restart
    ),
    label = "swipeOffset"
  )
  val alpha = 1f - (offsetY / targetOffsetPx).coerceIn(ZERO.toFloat(), 1f)
  Row(
    modifier = Modifier
      .padding(EIGHT_DP),
    verticalAlignment = Alignment.CenterVertically,
    horizontalArrangement = Arrangement.Center
  ) {
    Icon(
      painter = painterResource(id = drawable.ic_swipe_down),
      contentDescription = null,
      modifier = Modifier
        .offset { IntOffset(x = 0, y = offsetY.roundToInt()) }
        .size(TWENTY_DP)
        .alpha(alpha),
      tint = MaterialTheme.colorScheme.onSurfaceVariant
    )
    Spacer(Modifier.width(SIX_DP))
    Text(
      text = stringResource(string.swipe_down_to_scan_storage),
      textAlign = TextAlign.Center,
      style = MaterialTheme.typography.bodySmall.copy(
        color = MaterialTheme.colorScheme.onSurfaceVariant
      ),
      modifier = Modifier.semantics {
        testTag = SHOW_SWIPE_DOWN_TO_SCAN_FILE_SYSTEM_TEXT_TESTING_TAG
      }
    )
  }
}

const val SWIPE_DOWN_IMAGE_ANIMATION_TIME = 2000
