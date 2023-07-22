package com.wororn.storyapp

import androidx.lifecycle.LiveData
import androidx.paging.PagingData
import androidx.paging.PagingSource
import androidx.paging.PagingState
import com.wororn.storyapp.componen.response.TabStoriesItem
import com.wororn.storyapp.tools.Status


class StoryPagingSource : PagingSource<Int, LiveData<List<TabStoriesItem>>>() {
    companion object {
        fun snapshot(items: List<TabStoriesItem>): PagingData<TabStoriesItem> {
            return PagingData.from(items)
        }

    }
    override fun getRefreshKey(state: PagingState<Int, LiveData<List<TabStoriesItem>>>): Int {
        return 0
    }
    override suspend fun load(params: LoadParams<Int>): LoadResult<Int, LiveData<List<TabStoriesItem>>> {
        return LoadResult.Page(emptyList(), 0, 1)
    }
}
