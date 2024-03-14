package com.comunidadedevspace.taskbeats


import android.app.Activity
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.View
import android.widget.LinearLayout
import androidx.activity.result.ActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.recyclerview.widget.RecyclerView
import androidx.room.Room
import com.google.android.material.floatingactionbutton.FloatingActionButton
import com.google.android.material.snackbar.Snackbar
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers.IO
import kotlinx.coroutines.launch
import java.io.Serializable

class MainActivity : AppCompatActivity() {


    private var taskList= arrayListOf(
        Task(0,"title1", "Desc1"),
        Task(1,"title3", "Desc3"),
    )
    private lateinit var ctnContent: LinearLayout
    private val adapter: TaskListAdapter=TaskListAdapter(::onListItemClicked)

    private val startForResult= registerForActivityResult(
        ActivityResultContracts.StartActivityForResult()
    ) {  result:ActivityResult ->
        if (result.resultCode== Activity.RESULT_OK) {
            val data=result.data
            val taskAction=data?.getSerializableExtra(TASK_ACTION_RESULT) as TaskAction
            val task:Task=taskAction.task
            if (taskAction.actionType == ActionType.DELETE.name ) {

                val newList= arrayListOf<Task>()
                    .apply {
                        addAll(taskList)
                    }
                newList.remove(task)

                showMessage(ctnContent, "Item deleted ${task.title}")
                if(newList.size==0){
                    ctnContent.visibility=View.VISIBLE
                }
                adapter.submitList(newList)
                taskList=newList
            } else if (taskAction.actionType == ActionType.CREATE.name) {
                    val newList= arrayListOf<Task>()
                        .apply { addAll(taskList)
                        }
                newList.add(task)

                showMessage(ctnContent, "Item added ${task.title}")
                adapter.submitList(newList)
                taskList=newList
            }else if (taskAction.actionType == ActionType.UPDATE.name){

                val tempEmtyList = arrayListOf<Task>()
                taskList.forEach {
                    if (it.id == task.id){
                        val newItem = Task(
                                it.id,task.title,task.description)
                        tempEmtyList.add(newItem)
                    }else{
                        tempEmtyList.add(it)
                    }
                }
                showMessage(ctnContent, "Item updated ${task.title}")
                adapter.submitList(tempEmtyList)
                taskList = tempEmtyList

            }
        }
    }
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_task_list)

        val dataBase = Room.databaseBuilder(
            applicationContext,
            AppDataBase::class.java,"taskbeats-database"
        ).build()

        val dao= dataBase.taskDao()
        val task= Task(title="Academia", description = "Treino de corrida")

        CoroutineScope(IO).launch {
            dao.insert(task)
        }

        ctnContent =findViewById(R.id.ctn_content)
        adapter.submitList(taskList)

        val rvTask: RecyclerView=findViewById(R.id.rv_task_list)
        rvTask.adapter=adapter

        val fab= findViewById<FloatingActionButton>(R.id.fab_add)
        fab.setOnClickListener {
            openTaskListFetail(null)
        }
    }

    private fun showMessage(view:View, message:String){
        Snackbar.make(view,message,Snackbar.LENGTH_LONG)
            .setAction("Action", null)
            .show()
    }
    private fun onListItemClicked(task:Task) {
        openTaskListFetail(task)
    }
    private fun openTaskListFetail(task:Task?) {
        val intent=TaskDetailActivity.start(this, task)
        startForResult.launch(intent)
    }
}
enum class ActionType {
    DELETE,
    UPDATE,
    CREATE
}
data class TaskAction(
    val task:Task,
    val actionType: String
):Serializable

const val TASK_ACTION_RESULT="TASK_ACTION_RESULT"