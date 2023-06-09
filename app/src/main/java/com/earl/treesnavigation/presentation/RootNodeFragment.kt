package com.earl.treesnavigation.presentation

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.lifecycle.lifecycleScope
import com.earl.treesnavigation.databinding.FragmentRootNodeBinding
import com.earl.treesnavigation.domain.models.ChildNode
import com.earl.treesnavigation.presentation.utils.BaseFragment
import com.earl.treesnavigation.presentation.utils.ChildsRecyclerViewAdapter
import com.earl.treesnavigation.presentation.utils.Nodes
import com.earl.treesnavigation.presentation.utils.OnChildClickListener
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach

@AndroidEntryPoint
class RootNodeFragment : BaseFragment<FragmentRootNodeBinding>(), OnChildClickListener {

    override fun viewBinding(
        inflater: LayoutInflater,
        container: ViewGroup?
    ) = FragmentRootNodeBinding.inflate(inflater, container, false)

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        viewModel.fetchAllNodesFromDb()
        updateCurrentNodeParent(Nodes.root)
        initRecyclerAdapter()
        binding.addChild.setOnClickListener {
            addChildNode(Nodes.root)
        }
    }

    private fun initRecyclerAdapter() {
        val adapter = ChildsRecyclerViewAdapter(this)
        binding.childsRecycler.adapter = adapter
        viewModel.childs.onEach { list ->
            adapter.submitList(list.filter { it.parent == Nodes.root })
        }.launchIn(lifecycleScope)
    }

    override fun onChildNavigateClick(childNode: ChildNode) {
        navigate(childNode.name, Nodes.root)
    }

    override fun onChildRemoveClick(childName: ChildNode) {
        viewModel.removeChild(childName)
    }

    override fun onShowChildsBtnClick(childNode: ChildNode, newList: (List<ChildNode>) -> Unit) {
        val list = viewModel.childs.value.filter { it.parent == childNode.name }
        newList(list)
    }

    override fun onHideChildsBtnClick(childNode: ChildNode, list: (List<ChildNode>) -> Unit) {
        viewModel.findAllChildsOfNode(childNode) { readyList ->
            list(readyList)
        }
    }

    companion object {
        fun newInstance(name: String) = RootNodeFragment().apply {
            arguments = Bundle().apply {
                putString(Nodes.nodeName, name)
            }
        }
    }
}