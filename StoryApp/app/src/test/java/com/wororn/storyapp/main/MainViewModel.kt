package com.wororn.storyapp.main

import androidx.arch.core.executor.testing.InstantTaskExecutorRule
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.liveData
import androidx.paging.AsyncPagingDataDiffer
import androidx.paging.PagingData
import androidx.recyclerview.widget.ListUpdateCallback
import com.wororn.storyapp.DataDummy
import com.wororn.storyapp.MainDispatcherRule
import com.wororn.storyapp.StoryPagingSource
import com.wororn.storyapp.adapter.TabStoriesAdapter
import com.wororn.storyapp.componen.repository.StoriesRepository
import com.wororn.storyapp.componen.repository.UsersRepository
import com.wororn.storyapp.componen.response.DataStoriesResponse
import com.wororn.storyapp.componen.response.TabStoriesItem
import com.wororn.storyapp.getOrAwaitValue
import com.wororn.storyapp.interfaces.main.MainViewModel
import com.wororn.storyapp.interfaces.stories.StoriesViewModel
import com.wororn.storyapp.tools.Status
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.runTest
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.MultipartBody
import okhttp3.RequestBody.Companion.toRequestBody
import org.junit.Assert
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.mockito.Mock
import org.mockito.Mockito
import org.mockito.MockitoAnnotations
import org.mockito.junit.MockitoJUnitRunner

@ExperimentalCoroutinesApi
@RunWith(MockitoJUnitRunner::class)
class MainViewModelTest {
    @get:Rule
    val instantExecutorRule = InstantTaskExecutorRule()

    @get:Rule
    val mainDispatcherRules = MainDispatcherRule()

    @Mock
    private lateinit var usersRepository: UsersRepository
    @Mock
    private lateinit var storiesRepository: StoriesRepository

    private lateinit var storiesViewModel: StoriesViewModel
    private lateinit var mainViewModel: MainViewModel

    private val dummyStory = DataDummy.generateDummyTabStoriesItem()
    private val dummyResponse = DataDummy.generateDummyDataStoriesResponse()

    private val token =
        "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9.eyJ1c2VySWQiOiJ1c2VyLTF4dHZNakVNeWJ1MkM5N1MiLCJpYXQiOjE2ODg5NDQyNzd9.6S3FEm8qih32bH8zTZtU3qSww4wjYFv74Qr6I_WGsUA"

    @Before
    @Throws(Exception::class)
    fun setUp() {
        MockitoAnnotations.openMocks(this)
        storiesViewModel = StoriesViewModel(usersRepository, storiesRepository)
        mainViewModel = MainViewModel(usersRepository, storiesRepository)
    }

    @Test
    fun `when Get Story Should Not Null and Return Data`() = runTest {

        val data: PagingData<TabStoriesItem> = StoryPagingSource.snapshot(dummyStory)
        val expectedQuote = MutableLiveData<PagingData<TabStoriesItem>>()
        expectedQuote.value = data
        Mockito.`when`(storiesRepository.listStory(token)).thenReturn(expectedQuote)

        val mainViewModel = MainViewModel(usersRepository, storiesRepository)
        val actualQuote: PagingData<TabStoriesItem> = mainViewModel.listStory(token).getOrAwaitValue()

        val differ = AsyncPagingDataDiffer(
            diffCallback = TabStoriesAdapter.DIFF_CALLBACK,
            updateCallback = noopListUpdateCallback,
            workerDispatcher = Dispatchers.Main,
        )
        differ.submitData(actualQuote)

        Assert.assertNotNull(differ.snapshot())
        Assert.assertEquals(dummyStory.size, differ.snapshot().size)
        Assert.assertEquals(dummyStory[0], differ.snapshot()[0])
    }

    @Test
    fun `when Get Story Empty Should Return No Data`() = runTest {
        val data: PagingData<TabStoriesItem> = StoryPagingSource.snapshot(emptyList())
        val expectedQuote =  MutableLiveData<PagingData<TabStoriesItem>>()
        expectedQuote.value = data
        Mockito.`when`(storiesRepository.listStory(token)).thenReturn(expectedQuote)

        val mainViewModel = MainViewModel(usersRepository, storiesRepository)
        val actualQuote: PagingData<TabStoriesItem> = mainViewModel.listStory(token).getOrAwaitValue()

        val differ = AsyncPagingDataDiffer(
            diffCallback = TabStoriesAdapter.DIFF_CALLBACK,
            updateCallback = noopListUpdateCallback,
            workerDispatcher = Dispatchers.Main,
        )
        differ.submitData(actualQuote)
        Assert.assertEquals(0, differ.snapshot().size)
    }
}

val noopListUpdateCallback = object : ListUpdateCallback {
    override fun onInserted(position: Int, count: Int) {}
    override fun onRemoved(position: Int, count: Int) {}
    override fun onMoved(fromPosition: Int, toPosition: Int) {}
    override fun onChanged(position: Int, count: Int, payload: Any?) {}
}
