package com.wororn.storyapp

import com.wororn.storyapp.componen.response.*
import com.wororn.storyapp.componen.response.TabStoriesItem

object DataDummy {

    fun generateDummyTabStoriesItem(): List<TabStoriesItem> {
        val items: MutableList<TabStoriesItem> = arrayListOf()
        for (i in 0..100) {
            val quote = TabStoriesItem(
                i.toString(),
                "https://story-api.dicoding.dev/images/stories/photos-1689064032691_Ja3qUgy1.jpg + $i",
                "2023-07-11T08:27:12.692Z + $i",
                "woro@gmail.com + $i",
                "test + $i",
                i.toDouble(),
                i.toDouble(),
            )
            items.add(quote)
        }
        return items
    }

    fun generateDummyDataStoriesResponse(): DataStoriesResponse {
        return DataStoriesResponse(
            false,
            "success",
        )
    }

    fun generateDummyResponseLoginSuccess(): LoginResponse {
        val loginResult = LoginResult(
            "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9.eyJ1c2VySWQiOiJ1c2VyLTF4dHZNakVNeWJ1MkM5N1MiLCJpYXQiOjE2ODg5NDQyNzd9.6S3FEm8qih32bH8zTZtU3qSww4wjYFv74Qr6I_WGsUA",
            "story-T5zJsPvyh36V4E2T",
            "woro@gmail.com",

        )
        return LoginResponse(
            loginResult,
            error = false,
            message = "200"
        )
    }

    fun generateDummyResponseRegisterSuccess(): RegisterResponse {
        return RegisterResponse(
            error = false,
            message = "200"
        )
    }

}