package com.wororn.storyapp.interfaces.map

import androidx.lifecycle.ViewModel
import com.wororn.storyapp.componen.repository.StoriesRepository

class MapViewModel (private val storiesRepository: StoriesRepository) : ViewModel() {
    fun getAllMapData(token: String,location:Int) = storiesRepository.getMapData(token,location)

}