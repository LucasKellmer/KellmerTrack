package br.com.example.kellmertrack.services.tasks

import android.content.Context
import androidx.work.BackoffPolicy
import androidx.work.Constraints
import androidx.work.ExistingPeriodicWorkPolicy
import androidx.work.ExistingWorkPolicy
import androidx.work.NetworkType
import androidx.work.OneTimeWorkRequestBuilder
import androidx.work.PeriodicWorkRequestBuilder
import androidx.work.WorkInfo
import androidx.work.WorkManager
import androidx.work.WorkQuery
import androidx.work.WorkRequest
import androidx.work.workDataOf
import br.com.example.kellmertrack.services.worker.Workers
import dagger.hilt.android.qualifiers.ApplicationContext
import java.util.Collections
import java.util.concurrent.TimeUnit
import javax.inject.Inject
import kotlin.reflect.KClass

class TaskCreator @Inject constructor(
    @ApplicationContext private val context: Context
) {

    fun periodicJob(type: KClass<*>, uniqueTag: String, params:Map<String,Any>, interval:Long, timeUnit: TimeUnit = TimeUnit.MINUTES, initialDelay:Long=0, initialDelayTipe: TimeUnit = TimeUnit.MINUTES, auxTags:List<String> = Collections.emptyList(), cancelAll:Boolean=false, onlyConnected: Boolean=false){
        if(checkNeedsCreateJob(uniqueTag, cancelAll)){
            val constraints = Constraints.Builder()
            if(onlyConnected)
                constraints.setRequiredNetworkType(NetworkType.CONNECTED)
            val request = PeriodicWorkRequestBuilder<Workers>(interval, timeUnit)
                .setInputData(workDataOf("jobType" to type.simpleName,  *params.entries.map { Pair(it.key, it.value) }.toTypedArray()))
                .setInitialDelay(initialDelay, initialDelayTipe)
                .setConstraints(constraints.build())
                .addTag(uniqueTag)
            auxTags.forEach { request.addTag(it) }
            WorkManager.getInstance(context).enqueueUniquePeriodicWork(uniqueTag, ExistingPeriodicWorkPolicy.KEEP, request.build())
        }
    }

    fun uniqueRequest(type: KClass<*>, uniqueTag:String, params:Map<String,Any>, initialDelay:Long=0, initialDelayType: TimeUnit = TimeUnit.SECONDS, auxTags:List<String> = Collections.emptyList(), cancelAll:Boolean=false, onlyConnected:Boolean=true) {
        if(checkNeedsCreateJob(uniqueTag, cancelAll)) {
            val constraints = Constraints.Builder()
            if(onlyConnected)
                constraints.setRequiredNetworkType(NetworkType.CONNECTED)

            val request = OneTimeWorkRequestBuilder<Workers>()
                .setInputData(
                    workDataOf(
                        "jobType" to type.simpleName,
                        *params.entries.map { Pair(it.key, it.value) }.toTypedArray()
                    )
                )
                .setBackoffCriteria(
                    BackoffPolicy.LINEAR,
                    WorkRequest.MIN_BACKOFF_MILLIS,
                    TimeUnit.MILLISECONDS
                )
                .setInitialDelay(initialDelay, initialDelayType)
                .setConstraints(constraints.build())
                .addTag(uniqueTag)
            auxTags.forEach { request.addTag(it) }
            WorkManager.getInstance(context)
                .enqueueUniqueWork(uniqueTag, ExistingWorkPolicy.KEEP, request.build())
        }
    }

    private fun checkNeedsCreateJob(uniqueTag: String, cancelAll: Boolean):Boolean{
        val workInstance = WorkManager.getInstance(context)
        val activeWorks = workInstance.getWorkInfos(WorkQuery.Builder.fromStates(listOf(WorkInfo.State.ENQUEUED, WorkInfo.State.RUNNING)).build()).get().distinct()
        val existThisJob = activeWorks.filter { workInfo -> workInfo.tags.filter { tag -> tag == uniqueTag }.count() > 0 }.count()
        if(existThisJob > 0)
            return false
        if(cancelAll) {
            val toCancel = activeWorks.filter { workInfo -> workInfo.tags.filter { it == "cancelable" }.count() > 0 }
            toCancel.forEach { workInstance.cancelWorkById(it.id) }
        }
        return true
    }
}