package ru.pl.photogallery

import android.content.Context
import android.net.Uri
import android.os.Bundle
import android.util.Log
import android.view.*
import android.view.inputmethod.InputMethodManager
import androidx.activity.OnBackPressedCallback
import androidx.appcompat.widget.SearchView
import androidx.core.view.MenuItemCompat
import androidx.core.view.MenuProvider
import androidx.core.view.isVisible
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import androidx.navigation.fragment.findNavController
import androidx.paging.LoadState
import androidx.paging.PagingData
import androidx.recyclerview.widget.GridLayoutManager
import androidx.work.*
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch
import ru.pl.photogallery.worker.PollWorker
import ru.pl.photogallery.databinding.FragmentPhotoGalleryBinding
import java.util.concurrent.TimeUnit


private const val TAG = "PhotoGalleryFragment"
private const val POLL_WORK = "POLL_WORK"

class PhotoGalleryFragment : Fragment() {

    private var searchView: SearchView? = null
    private var pollingMenuItem: MenuItem? = null

    private var _binding: FragmentPhotoGalleryBinding? = null
    private val binding: FragmentPhotoGalleryBinding
        get() = checkNotNull(_binding) {
            "Cannot access binding because it is null. Is view visible?"
        }

    private val photoGalleryViewModel: PhotoGalleryViewModel by viewModels()

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {

        _binding = FragmentPhotoGalleryBinding.inflate(inflater, container, false)
        binding.photoGrid.layoutManager = GridLayoutManager(context, 3)

        setUpBackLogic()

        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        //paging
        val pagingAdapter = PhotoPagingAdapter { startImageWebView(it) }
        binding.photoGrid.adapter = pagingAdapter

        viewLifecycleOwner.lifecycleScope.launch {
            viewLifecycleOwner.repeatOnLifecycle(Lifecycle.State.STARTED) {
                photoGalleryViewModel.uiState.collect { state ->
                    searchView?.setQuery(state.query, false)
                    updatePollingState(state.isPolling)
                }
            }
        }

        //paging
        viewLifecycleOwner.lifecycleScope.launch {
            viewLifecycleOwner.repeatOnLifecycle(Lifecycle.State.STARTED) {
                photoGalleryViewModel.galleryItems.collectLatest {
                    Log.d(TAG, "inside photoGalleryViewModel.galleryItems.collectLatest")
                    pagingAdapter.submitData(it)
                }
            }
        }


        //paging progress
        lifecycleScope.launch {
            viewLifecycleOwner.repeatOnLifecycle(Lifecycle.State.STARTED) {
                pagingAdapter.loadStateFlow.collect { loadState ->
                    binding.apply {
                        prependProgress.isVisible = loadState.source.prepend is LoadState.Loading
                        appendProgress.isVisible = loadState.source.append is LoadState.Loading
                        progressBar.isVisible = loadState.source.refresh is LoadState.Loading

                        val noConnection = loadState.source.refresh is LoadState.Error
                        tryAgainButton.isVisible = noConnection
                        photoGrid.visibility =
                            if (loadState.source.refresh is LoadState.Loading || noConnection)
                                View.GONE
                            else
                                View.VISIBLE

                        tryAgainButton.setOnClickListener {
                            pagingAdapter.retry()
                        }
                    }
                }
            }
        }

        setUpMenu()
    }

    private fun startImageWebView(photoPageUri: Uri) {
        findNavController().navigate(
            PhotoGalleryFragmentDirections.showPhoto(
                photoPageUri
            )
        )
        //другой вариает запуск хром таб
        /*CustomTabsIntent.Builder()
            //deprecated:
//                            .setToolbarColor(
//                                ContextCompat.getColor(
//                                    requireContext(),
//                                    R.color.design_default_color_primary
//                                )
//                            )
            .setDefaultColorSchemeParams(
                CustomTabColorSchemeParams.Builder()
                    .setToolbarColor(
                        resources.getColor(
                            R.color.design_default_color_primary,
                            null
                        )
                    ).build()
            )
            .setShowTitle(true)
            .build()
            .launchUrl(requireContext(), photoPageUri)*/
    }


    private fun setUpMenu() {
        val menuHost = requireActivity()
        menuHost.addMenuProvider(object : MenuProvider {
            override fun onCreateMenu(menu: Menu, menuInflater: MenuInflater) {
                menuInflater.inflate(R.menu.fragment_photo_gallery, menu)

                val searchItem: MenuItem = menu.findItem(R.id.menu_item_search)
                searchView = searchItem.actionView as? SearchView
                pollingMenuItem = menu.findItem(R.id.menu_item_toggle_polling)

                searchView?.setOnQueryTextListener(object : SearchView.OnQueryTextListener {
                    override fun onQueryTextSubmit(query: String?): Boolean {
                        Log.d(TAG, "Query text submit: $query")
                        searchView?.onActionViewCollapsed()
                        photoGalleryViewModel.setQuery(query ?: "")
                        hideKeyboard()
                        return true
                    }

                    override fun onQueryTextChange(newText: String?): Boolean {
                        Log.d(TAG, "Query text change: $newText")
                        return false
                    }
                })
            }

            override fun onMenuItemSelected(menuItem: MenuItem): Boolean {
                return when (menuItem.itemId) {
                    R.id.menu_item_clear -> {
                        photoGalleryViewModel.setQuery("")
                        true
                    }
                    R.id.menu_item_toggle_polling -> {
                        photoGalleryViewModel.toggleIsPolling()
                        true
                    }
                    else -> false
                }
            }
        }, viewLifecycleOwner, Lifecycle.State.STARTED)
    }

    private fun updatePollingState(isPolling: Boolean) {
        val toggleItemTitle = if (isPolling) {
            R.string.stop_polling
        } else {
            R.string.start_polling
        }
        pollingMenuItem?.setTitle(toggleItemTitle)

        if (isPolling) {
            val constraints = Constraints.Builder()
                .setRequiredNetworkType(NetworkType.UNMETERED)
                .build()
            val periodicRequest = PeriodicWorkRequestBuilder<PollWorker>(15, TimeUnit.MINUTES)
                .setConstraints(constraints)
                .build()
            WorkManager.getInstance(requireContext()).enqueueUniquePeriodicWork(
                POLL_WORK,
                ExistingPeriodicWorkPolicy.KEEP,
                periodicRequest
            )
        } else {
            WorkManager.getInstance(requireContext()).cancelUniqueWork(POLL_WORK)
        }
    }

    private fun hideKeyboard() {
        val view = activity?.currentFocus
        val imm: InputMethodManager =
            activity?.getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
        imm.hideSoftInputFromWindow(view?.windowToken, 0)
    }

    private fun setUpBackLogic() {
        requireActivity().onBackPressedDispatcher.addCallback(
            viewLifecycleOwner,
            object : OnBackPressedCallback(true) {
                override fun handleOnBackPressed() {
                    if (searchView?.isIconified == false) searchView?.onActionViewCollapsed()
                    else activity?.finish()
                }
            })
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

}