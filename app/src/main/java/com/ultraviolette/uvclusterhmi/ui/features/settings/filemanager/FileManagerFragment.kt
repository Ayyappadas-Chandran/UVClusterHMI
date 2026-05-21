
package com.ultraviolette.uvclusterhmi.ui.features.settings.filemanager

import android.content.*
import android.os.*
import android.util.Log
import android.view.*
import android.view.inputmethod.EditorInfo
import android.view.inputmethod.InputMethodManager
import android.widget.*
import androidx.activity.addCallback
import androidx.appcompat.app.AlertDialog
import androidx.core.content.FileProvider
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.*
import androidx.work.*
import com.google.android.material.appbar.MaterialToolbar
import com.google.android.material.floatingactionbutton.FloatingActionButton
import com.ultraviolette.uvclusterhmi.R
import java.io.File
import java.util.concurrent.TimeUnit
import android.text.TextWatcher
import android.text.Editable


class FileManagerFragment : Fragment() {

    private val TAG = "FileManager"

    private lateinit var recyclerView: RecyclerView
    private lateinit var toolbar: MaterialToolbar
    private lateinit var fab: FloatingActionButton
    private lateinit var searchBar: EditText

    private lateinit var progressBar: ProgressBar
    private lateinit var progressText: TextView

    private lateinit var adapter: FileAdapter

    private val pathStack = mutableListOf<String>()
    private val currentList = mutableListOf<FileNode>()

    private var selectAllState = false

    data class FileNode(
        var name: String,
        var fullPath: String,
        val isDirectory: Boolean,
        var isSelected: Boolean = false
    )

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ) = inflater.inflate(R.layout.fragment_file_manager, container, false)

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {

        Log.d(TAG, "onViewCreated called")

        recyclerView = view.findViewById(R.id.recyclerView)
        toolbar = view.findViewById(R.id.topBar)
        fab = view.findViewById(R.id.fab)
        searchBar = view.findViewById(R.id.searchBar)

        progressBar = view.findViewById(R.id.storageProgressBar)
        progressText = view.findViewById(R.id.storageText)

        setupRecycler()
        setupToolbar()
        setupFab()
        setupSearch()

        adapter = FileAdapter(currentList)
        recyclerView.adapter = adapter

        loadDirectory("/")
        updateStorageProgress()

        if (!WorkManager.isInitialized()) {
            Log.d(TAG, "Initializing WorkManager")
            WorkManager.initialize(
                requireContext().applicationContext,
                Configuration.Builder().build()
            )
        }

        CleanupScheduler.schedule(requireContext())

        requireActivity().onBackPressedDispatcher.addCallback(viewLifecycleOwner) {
            if (pathStack.size > 1) {
                pathStack.removeAt(pathStack.lastIndex)
                loadDirectory(pathStack.last())
            } else {
                requireActivity().finish()
            }
        }
    }

    // ================= STORAGE =================
    private fun updateStorageProgress() {
        val stat = StatFs(Environment.getDataDirectory().path)
        val total = stat.totalBytes.toDouble()
        val used = total - stat.availableBytes
        val percent = ((used / total) * 100).toInt()

        Log.d(TAG, "Storage Used: $percent%")

        progressBar.progress = percent
        progressText.text = "Storage Used: $percent%"
    }

    // ================= LOAD =================
    private fun loadDirectory(path: String) {

        Log.d(TAG, "Loading directory: $path")

        Thread {
            val dir = File(path)

            val files = dir.listFiles()?.map {
                FileNode(it.name, it.absolutePath, it.isDirectory)
            } ?: emptyList()

            Log.d(TAG, "Files loaded: ${files.size}")

            val sorted = files.sortedWith(
                compareBy<FileNode>(
                    { !it.isDirectory },
                    { it.name.lowercase() }
                )
            )

            if (pathStack.isEmpty() || pathStack.last() != path) {
                pathStack.add(path)
            }

            currentList.clear()
            currentList.addAll(sorted)

            requireActivity().runOnUiThread {
                adapter.notifyDataSetChanged()
            }

        }.start()
    }

    private fun setupRecycler() {
        recyclerView.layoutManager = LinearLayoutManager(context)
    }

    // ================= SEARCH =================
    private fun setupSearch() {

        searchBar.addTextChangedListener(object : android.text.TextWatcher {
            override fun afterTextChanged(s: Editable?) {}
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
                Log.d(TAG, "Search typing: $s")
                performSearch(s.toString())
            }
        })

        searchBar.setOnEditorActionListener { _, actionId, event ->

            if (actionId == EditorInfo.IME_ACTION_SEARCH ||
                (event != null && event.keyCode == KeyEvent.KEYCODE_ENTER)
            ) {

                Log.d(TAG, "Search submitted: ${searchBar.text}")

                performSearch(searchBar.text.toString())

                val imm = requireContext().getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
                imm.hideSoftInputFromWindow(searchBar.windowToken, 0)

                true
            } else false
        }
    }

    private fun performSearch(query: String) {
        Log.d(TAG, "Performing search: $query")

        if (query.isBlank()) {
            adapter.updateList(currentList)
            return
        }

        val filtered = currentList.filter {
            it.name.contains(query, ignoreCase = true)
        }

        Log.d(TAG, "Search result count: ${filtered.size}")

        adapter.updateList(filtered)
    }

    // ================= SORT =================
    private fun sortAZ() {
        Log.d(TAG, "Sorting A-Z")
        val sorted = currentList.sortedBy { it.name.lowercase() }
        adapter.updateList(sorted)
    }

    private fun sortByDate() {
        Log.d(TAG, "Sorting by date")
        val sorted = currentList.sortedByDescending {
            File(it.fullPath).lastModified()
        }
        adapter.updateList(sorted)
    }

    // ================= TOOLBAR =================
    private fun setupToolbar() {

        toolbar.setNavigationOnClickListener {
            Log.d(TAG, "Toolbar back clicked")
            findNavController().navigate(R.id.debugFragment)
        }

        toolbar.setOnClickListener {
            Log.d(TAG, "Toggle select all")
            toggleSelectAll()
        }

        toolbar.setOnLongClickListener {
            Log.d(TAG, "Toolbar long press → sort A-Z")
            sortAZ()
            true
        }
    }

    // ================= FAB =================
    private fun setupFab() {
        fab.setOnClickListener {

            Log.d(TAG, "FAB clicked")

            val options = arrayOf("Delete", "Sort A-Z", "Sort Date")

            AlertDialog.Builder(requireContext())
                .setItems(options) { _, which ->
                    when (which) {
                        0 -> {
                            Log.d(TAG, "Delete selected clicked")
                            deleteSelected()
                        }
                        1 -> sortAZ()
                        2 -> sortByDate()
                    }
                }
                .show()
        }
    }

    // ================= DELETE =================
    private fun deleteSelected() {

        Log.d(TAG, "Triggering WorkManager cleanup")

        val request = OneTimeWorkRequestBuilder<LogCleanupWorker>().build()

        WorkManager.getInstance(requireContext()).enqueue(request)

        Toast.makeText(context, "Cleanup started", Toast.LENGTH_SHORT).show()
    }

    private fun toggleSelectAll() {
        selectAllState = !selectAllState
        Log.d(TAG, "Select all toggled: $selectAllState")

        currentList.forEach { it.isSelected = selectAllState }
        adapter.notifyDataSetChanged()
    }

    // ================= ADAPTER =================
    inner class FileAdapter(private val files: MutableList<FileNode>) :
        RecyclerView.Adapter<FileAdapter.VH>() {

        inner class VH(view: View) : RecyclerView.ViewHolder(view) {
            val name: TextView = view.findViewById(R.id.fileName)
            val icon: ImageView = view.findViewById(R.id.fileIcon)
            val check: CheckBox = view.findViewById(R.id.fileCheck)
        }

        override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): VH {
            return VH(layoutInflater.inflate(R.layout.item_file, parent, false))
        }

        override fun getItemCount() = files.size

        override fun onBindViewHolder(holder: VH, position: Int) {

            val node = files[position]

            holder.name.text = node.name
            holder.check.isChecked = node.isSelected

            holder.itemView.setOnClickListener {
                Log.d(TAG, "Clicked: ${node.fullPath}")

                if (node.isDirectory) loadDirectory(node.fullPath)
                else openFile(node.fullPath)
            }

            holder.itemView.setOnLongClickListener {
                node.isSelected = !node.isSelected
                Log.d(TAG, "Selection changed: ${node.name} → ${node.isSelected}")
                notifyItemChanged(position)
                true
            }
        }

        fun updateList(newList: List<FileNode>) {
            Log.d(TAG, "Adapter update list size: ${newList.size}")
            files.clear()
            files.addAll(newList)
            notifyDataSetChanged()
        }
    }

    private fun openFile(path: String) {
        Log.d(TAG, "Opening file: $path")

        val file = File(path)
        val uri = FileProvider.getUriForFile(
            requireContext(),
            requireContext().packageName + ".provider",
            file
        )

        val intent = Intent(Intent.ACTION_VIEW)
        intent.setDataAndType(uri, "*/*")
        intent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)

        startActivity(intent)
    }
}

//
// ================= WORKER =================
//
class LogCleanupWorker(
    context: Context,
    params: WorkerParameters
) : Worker(context, params) {

    private val TAG = "CleanupWorker"

    override fun doWork(): Result {

        Log.d(TAG, "🚀 Worker started")

        val stat = StatFs(Environment.getDataDirectory().path)
        val usedPct =
            ((stat.totalBytes - stat.availableBytes).toDouble() / stat.totalBytes * 100)

        Log.d(TAG, "Storage Used: $usedPct%")

        if (usedPct <= 80) {
            Log.d(TAG, "Skipping cleanup — below threshold")
            return Result.success()
        }

        val intent = Intent("com.example.database.ACTION_CLEANUP_LOGS").apply {
            setPackage("com.example.database")
            putExtra("used_pct", usedPct)
        }

        try {
            //applicationContext.sendBroadcastAsUser(intent, UserHandle.ALL)
            Log.d(TAG, "✅ Broadcast SENT successfully")
        } catch (e: Exception) {
            Log.e(TAG, "❌ Broadcast FAILED", e)
        }

        return Result.success()
    }
}

//
// ================= SCHEDULER =================
//
object CleanupScheduler {

    private const val TAG = "CleanupScheduler"

    fun schedule(context: Context) {

        Log.d(TAG, "Scheduling periodic cleanup")

        val request = PeriodicWorkRequestBuilder<LogCleanupWorker>(
            24, TimeUnit.HOURS
        ).build()

        WorkManager.getInstance(context)
            .enqueueUniquePeriodicWork(
                "LogCleanup",
                ExistingPeriodicWorkPolicy.KEEP,
                request
            )
    }
}
