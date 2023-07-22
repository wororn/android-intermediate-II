package com.wororn.storyapp.register

import androidx.arch.core.executor.testing.InstantTaskExecutorRule
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.liveData
import com.wororn.storyapp.DataDummy
import com.wororn.storyapp.MainDispatcherRule
import com.wororn.storyapp.componen.repository.UsersRepository
import com.wororn.storyapp.componen.response.RegisterResponse
import com.wororn.storyapp.getOrAwaitValue
import com.wororn.storyapp.interfaces.register.RegisterViewModel
import com.wororn.storyapp.tools.Status
import kotlinx.coroutines.ExperimentalCoroutinesApi
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
    private lateinit var registerViewModel: RegisterViewModel

    private val dummyResponseSuccess = DataDummy.generateDummyResponseRegisterSuccess()

    private val name ="woro@gmail.com"
    private val email ="woro@gmail.com"
    private val password ="Saya486!"

    @Before
    @Throws(Exception::class)
    fun setUp() {
        MockitoAnnotations.openMocks(this)
        registerViewModel = RegisterViewModel(usersRepository)
    }

    @Test
    fun `when Get Login Should Not Null and Return Success`() {

        val expectedNews = MutableLiveData<Status<RegisterResponse>>()

        expectedNews.value = Status.Done(dummyResponseSuccess)
        val result = liveData { emitSource(expectedNews) }
        Mockito.`when`(usersRepository.register(
            name,
            email,
            password
        )).thenReturn(result)

        val actualNews = registerViewModel.register(
            name,
            email,
            password
        ).getOrAwaitValue()

        Mockito.verify(usersRepository).register(
            name,
            email,
            password
        )
        Assert.assertNotNull(actualNews)
        Assert.assertTrue(actualNews is Status.Done)
        Assert.assertEquals(dummyResponseSuccess.message, (actualNews as Status.Done).data.message)
    }

}
