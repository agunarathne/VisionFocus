package com.visionfocus.ui.beacon

import android.Manifest
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AlertDialog
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import androidx.recyclerview.widget.LinearLayoutManager
import com.visionfocus.databinding.FragmentBeaconManagementBinding
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.launch

/**
 * Epic 10 Story 10.1: Beacon Management Screen.
 * Allows user to view, add, and delete paired Bluetooth beacons.
 */
@AndroidEntryPoint
class BeaconManagementFragment : Fragment() {

    private var _binding: FragmentBeaconManagementBinding? = null
    private val binding get() = _binding!!

    private val viewModel: BeaconManagementViewModel by viewModels()
    private lateinit var adapter: BeaconAdapter

    private val requestPermissionLauncher = registerForActivityResult(
        ActivityResultContracts.RequestMultiplePermissions()
    ) { permissions ->
        // Check if all required permissions are granted
        val allGranted = permissions.entries.all { it.value }
        if (allGranted) {
            showAddBeaconDialog()
        } else {
            Toast.makeText(
                context, 
                "Bluetooth and Location permissions are required to scan for tags.", 
                Toast.LENGTH_LONG
            ).show()
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentBeaconManagementBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        setupRecyclerView()
        setupObservers()
        setupListeners()
    }

    private fun setupRecyclerView() {
        adapter = BeaconAdapter(
            onDeleteClick = { beacon ->
                // Show delete confirmation
                AlertDialog.Builder(requireContext())
                    .setTitle("Delete Beacon")
                    .setMessage("Are you sure you want to delete ${beacon.name}?")
                    .setPositiveButton("Delete") { _, _ ->
                        viewModel.deleteBeacon(beacon)
                    }
                    .setNegativeButton("Cancel", null)
                    .show()
            },
            onItemClick = { beacon ->
                // Maybe show edit dialog? For now just toast
                Toast.makeText(requireContext(), "Selected: ${beacon.name}", Toast.LENGTH_SHORT).show()
            }
        )
        binding.beaconRecyclerView.layoutManager = LinearLayoutManager(context)
        binding.beaconRecyclerView.adapter = adapter
    }

    private fun setupListeners() {
        binding.scanFab.setOnClickListener {
            checkPermissionsAndScan()
        }
    }

    private fun checkPermissionsAndScan() {
        // Determine required permissions based on Android version
        val permissions = mutableListOf<String>()
        
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            permissions.add(Manifest.permission.BLUETOOTH_SCAN)
            permissions.add(Manifest.permission.BLUETOOTH_CONNECT)
        } else {
            permissions.add(Manifest.permission.ACCESS_FINE_LOCATION)
            permissions.add(Manifest.permission.ACCESS_COARSE_LOCATION)
        }

        val missingPermissions = permissions.filter {
            ContextCompat.checkSelfPermission(requireContext(), it) != PackageManager.PERMISSION_GRANTED
        }

        if (missingPermissions.isEmpty()) {
            showAddBeaconDialog()
        } else {
            requestPermissionLauncher.launch(permissions.toTypedArray())
        }
    }

    private fun showAddBeaconDialog() {
        val dialog = AddBeaconDialogFragment()
        dialog.show(childFragmentManager, AddBeaconDialogFragment.TAG)
    }

    private fun setupObservers() {
        viewLifecycleOwner.lifecycleScope.launch {
            viewLifecycleOwner.repeatOnLifecycle(Lifecycle.State.STARTED) {
                viewModel.savedBeacons.collect { beacons ->
                    adapter.submitList(beacons)
                    
                    if (beacons.isEmpty()) {
                        binding.emptyStateView.visibility = View.VISIBLE
                        binding.beaconRecyclerView.visibility = View.GONE
                    } else {
                        binding.emptyStateView.visibility = View.GONE
                        binding.beaconRecyclerView.visibility = View.VISIBLE
                    }
                }
            }
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
