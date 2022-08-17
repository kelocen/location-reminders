package dev.kelocen.reminders.locationreminders.reminderslist

import android.os.Bundle
import android.view.*
import android.view.View.OnClickListener
import androidx.databinding.DataBindingUtil
import androidx.navigation.fragment.NavHostFragment.findNavController
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout
import com.firebase.ui.auth.AuthUI
import dev.kelocen.reminders.R
import dev.kelocen.reminders.base.BaseFragment
import dev.kelocen.reminders.base.NavigationCommand
import dev.kelocen.reminders.databinding.FragmentRemindersListBinding
import dev.kelocen.reminders.locationreminders.ReminderDescriptionActivity
import dev.kelocen.reminders.locationreminders.savereminder.SaveReminderFragment
import dev.kelocen.reminders.utils.setDisplayHomeAsUpEnabled
import dev.kelocen.reminders.utils.setTitle
import dev.kelocen.reminders.utils.setup
import org.koin.androidx.viewmodel.ext.android.viewModel

class RemindersListFragment : BaseFragment() {
    override val viewModel: RemindersListViewModel by viewModel()
    private lateinit var binding: FragmentRemindersListBinding
    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?,
    ): View {
        binding = DataBindingUtil.inflate(
            inflater,
            R.layout.fragment_reminders_list,
            container,
            false
        )
        binding.viewModel = viewModel
        setHasOptionsMenu(true)
        setDisplayHomeAsUpEnabled(false)
        setTitle(getString(R.string.app_name))
        setupSwipeRefreshListener()
        return binding.root
    }

    /**
     * Configures a [SwipeRefreshLayout] listener for the reminder list.
     */
    private fun setupSwipeRefreshListener() {
        binding.refreshLayout.setOnRefreshListener {
            viewModel.loadReminders()
            binding.refreshLayout.isRefreshing = false
        }
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        binding.lifecycleOwner = this
        setupRecyclerView()
        setupAddReminderFab()
    }

    /**
     * Configures the [RecyclerView][R.id.remindersRecyclerView] using a [RemindersListAdapter].
     */
    private fun setupRecyclerView() {
        val adapter = RemindersListAdapter { reminder ->
            startActivity(ReminderDescriptionActivity.newIntent(requireContext(), reminder))
        }
        binding.remindersRecyclerView.setup(adapter)
    }

    /**
     * Configures an [OnClickListener] for the [Add Reminder][R.id.addReminderFAB].
     */
    private fun setupAddReminderFab() {
        binding.addReminderFAB.setOnClickListener {
            navigateToSaveReminder()
        }
    }

    override fun onResume() {
        super.onResume()
        viewModel.loadReminders()
    }

    /**
     * Use [NavigationCommand] to navigate to the [SaveReminderFragment].
     */
    private fun navigateToSaveReminder() {
        viewModel.navigationCommand.postValue(
            NavigationCommand.To(
                RemindersListFragmentDirections.toSaveReminder()
            )
        )
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        when (item.itemId) {
            R.id.logout -> {
                AuthUI.getInstance().signOut(requireContext())
                findNavController(this).popBackStack()
                return true
            }
        }
        return super.onOptionsItemSelected(item)
    }

    override fun onCreateOptionsMenu(menu: Menu, inflater: MenuInflater) {
        super.onCreateOptionsMenu(menu, inflater)
        inflater.inflate(R.menu.main_menu, menu)
    }
}