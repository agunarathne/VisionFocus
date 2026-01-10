package com.visionfocus.ui.beacon

import android.app.Dialog
import android.os.Bundle
import android.text.InputType
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.WindowManager
import android.widget.EditText
import android.widget.FrameLayout
import androidx.appcompat.app.AlertDialog
import androidx.fragment.app.DialogFragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import androidx.recyclerview.widget.LinearLayoutManager
import com.visionfocus.beacon.ScannedBeacon
import com.visionfocus.databinding.FragmentAddBeaconDialogBinding
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.launch

/**
 * Dialog for scanning and adding new Bluetooth beacons.
 */
@AndroidEntryPoint
class AddBeaconDialogFragment : DialogFragment() {

    private var _binding: FragmentAddBeaconDialogBinding? = null
    private val binding get() = _binding!!

    // Share ViewModel with parent fragment if possible, or use nav graph scope
    // For simplicity here, we'll try to get the parent's ViewModel or just a new instance 
    // but sharing is better so the parent list updates automatically.
    // However, requireParentFragment() only works if added via specific transaction methods.
    // We'll use requireParentFragment() assuming it's a child.
    private val viewModel: BeaconManagementViewModel by viewModels({ requireParentFragment() })
    
    private lateinit var adapter: ScannedBeaconAdapter

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentAddBeaconDialogBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        
        setupRecyclerView()
        setupObservers()
        
        binding.cancelButton.setOnClickListener {
            dismiss()
        }

        // Start scanning automatically
        viewModel.startScanning()
    }

    override fun onStart() {
        super.onStart()
        dialog?.window?.setLayout(
            WindowManager.LayoutParams.MATCH_PARENT,
            WindowManager.LayoutParams.WRAP_CONTENT
        )
    }

    private fun setupRecyclerView() {
        adapter = ScannedBeaconAdapter { beacon ->
            showNameInputDialog(beacon)
        }
        binding.scanRecyclerView.layoutManager = LinearLayoutManager(context)
        binding.scanRecyclerView.adapter = adapter
    }

    private fun setupObservers() {
        viewLifecycleOwner.lifecycleScope.launch {
            viewLifecycleOwner.repeatOnLifecycle(Lifecycle.State.STARTED) {
                launch {
                    viewModel.scannedBeacons.collect { beacons ->
                        adapter.submitList(beacons)
                        binding.emptyStateText.visibility = 
                            if (beacons.isEmpty()) View.VISIBLE else View.GONE
                    }
                }
                
                launch {
                    viewModel.isScanning.collect { isScanning ->
                        binding.scanProgress.visibility = 
                            if (isScanning) View.VISIBLE else View.INVISIBLE
                        
                        if (!isScanning && adapter.currentList.isEmpty()) {
                            binding.subtitleText.text = "Scan finished. No devices found."
                        } else if (isScanning) {
                            binding.subtitleText.text = "Bringing device close to your phone..."
                        }
                    }
                }
            }
        }
    }

    private fun showNameInputDialog(beacon: ScannedBeacon) {
        // Stop scanning when user selects a device
        viewModel.stopScanning()

        val input = EditText(requireContext()).apply {
            hint = "e.g., Keys, Wallet, Front Door"
            inputType = InputType.TYPE_CLASS_TEXT or InputType.TYPE_TEXT_FLAG_CAP_SENTENCES
            
            // Add margin
            val params = FrameLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT, 
                ViewGroup.LayoutParams.WRAP_CONTENT
            )
            params.setMargins(50, 0, 50, 0)
            layoutParams = params
        }
        
        // Wrap in a container for margins
        val container = FrameLayout(requireContext())
        container.addView(input)

        AlertDialog.Builder(requireContext())
            .setTitle("Name Your Tag")
            .setMessage("Give a friendly name to identify this tag later.")
            .setView(container)
            .setPositiveButton("Save") { _, _ ->
                val name = input.text.toString().trim()
                if (name.isNotEmpty()) {
                    viewModel.pairBeacon(beacon, name)
                    dismiss()
                }
            }
            .setNegativeButton("Cancel") { _, _ ->
                // Resume scanning?
                viewModel.startScanning()
            }
            .show()
    }

    override fun onDestroyView() {
        super.onDestroyView()
        viewModel.stopScanning()
        _binding = null
    }

    companion object {
        const val TAG = "AddBeaconDialog"
    }
}
