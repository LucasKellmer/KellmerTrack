package br.com.example.kellmertrack.services.worker

import android.content.Context
import android.util.Log
import androidx.hilt.work.HiltWorker
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import br.com.example.kellmertrack.TAG_TASK
import br.com.example.kellmertrack.services.tasks.JobCreator
import br.com.example.kellmertrack.services.tasks.TaskResult
import dagger.assisted.Assisted
import dagger.assisted.AssistedInject
import java.text.SimpleDateFormat
import java.util.Calendar

@HiltWorker
class Workers @AssistedInject constructor(
    @Assisted private val appContext: Context,
    @Assisted private val workerParameters: WorkerParameters,
    private val jobCreator: JobCreator
) : CoroutineWorker(appContext, workerParameters){

    private val dataFormatSql = SimpleDateFormat("yyyy-MM-dd HH:mm:ss")
    private val dataAtual = Calendar.getInstance()
    private val dataAtualFormatada = dataFormatSql.format(dataAtual.time)
    override suspend fun doWork(): Result {
        try{
            val jobType = workerParameters.inputData.getString("jobType")
            val task=jobCreator.createTaskTesteSensor(jobType!!)
            Log.d(TAG_TASK,"Inicio job -> $jobType ------ executado em -> $dataAtualFormatada")
            val job = task.runTaskAsync(workerParameters.inputData.keyValueMap)
            val result = job.await()
            Log.d(TAG_TASK,"Finalizando job -> $jobType")
            return when(result){
                TaskResult.RETRY -> Result.retry()
                TaskResult.SUCCESS -> Result.success()
                TaskResult.FAILURE -> Result.failure()
            }
        }catch (e:Exception){
            println(e)
            return Result.failure()
        }
    }
}