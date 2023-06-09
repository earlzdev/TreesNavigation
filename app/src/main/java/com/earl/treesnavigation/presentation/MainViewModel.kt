package com.earl.treesnavigation.presentation

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.earl.treesnavigation.domain.Interactor
import com.earl.treesnavigation.domain.models.ChildNode
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class MainViewModel @Inject constructor(
    private val interactor: Interactor
) : ViewModel() {

    private val _childs: MutableStateFlow<List<ChildNode>> = MutableStateFlow(emptyList())
    val childs : StateFlow<List<ChildNode>> = _childs.asStateFlow()

    private var childsListForNode = mutableListOf<ChildNode>()

    fun fetchAllNodesFromDb() {
        viewModelScope.launch(Dispatchers.IO) {
            _childs.value = interactor.fetchAllNodesFromLocalDb()
        }
    }

    fun addChild(child: ChildNode) {
        _childs.value += child
        viewModelScope.launch(Dispatchers.IO) {
            interactor.insertNodeIntoDb(child)
        }
    }

    fun addChildForNodeInDb(parent: String, child: String) {
        viewModelScope.launch(Dispatchers.IO) {
            interactor.updateChildsListForNode(parent, child)
        }
    }

    fun findAllChildsOfNode(node: ChildNode, callback: (List<ChildNode>) -> Unit) {
        val childsList = _childs.value.filter { it.parent == node.name }
        childsList.forEach { childNode ->
            if (childNode.childsNames.isNotEmpty()) {
                val childs = _childs.value.filter { it.parent == childNode.name }
                childsListForNode += childNode
                childs.forEach { findAllChildsOfNode(childNode, callback) }
            } else {
                childsListForNode += childNode
                callback(childsListForNode.toSet().toList())
            }
        }
    }

    fun removeChild(child: ChildNode) {
        val childsList = _childs.value.filter { it.parent == child.name }
        _childs.value -= child
        childsList.forEach { childNode ->
            if (childNode.childsNames.isNotEmpty()) {
                val childs = _childs.value.filter { it.parent == childNode.name }
                _childs.value -= childNode
                childs.forEach { removeChild(childNode) }
            } else {
                _childs.value -= childNode
            }
        }
        viewModelScope.launch(Dispatchers.IO) {
            interactor.deleteNodeFromLocalDb(child.name)
        }
    }
}