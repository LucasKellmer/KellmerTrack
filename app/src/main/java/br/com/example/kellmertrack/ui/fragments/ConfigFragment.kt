package br.com.example.kellmertrack.ui.fragments

import android.content.Context
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import androidx.work.WorkManager
import br.com.example.kellmertrack.R
import br.com.example.kellmertrack.TAG
import br.com.example.kellmertrack.TAG_TASK_SINC
import br.com.example.kellmertrack.core.Sistema
import br.com.example.kellmertrack.databinding.ConfigFragmentBinding
import br.com.example.kellmertrack.extensions.verificaConexao
import br.com.example.kellmertrack.local.model.mappers.SetupMapper
import br.com.example.kellmertrack.services.tasks.TaskCreator
import br.com.example.kellmertrack.services.tasks.TaskFirebase
import br.com.example.kellmertrack.ui.viewmodel.ConfigViewModel
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.launch

@AndroidEntryPoint
class ConfigFragment : Fragment() {
    private lateinit var binding : ConfigFragmentBinding
    private val viewModel by viewModels<ConfigViewModel>()
    private val routeController by lazy { findNavController() }
    private lateinit var context : Context
    private val progressDialog by lazy {
        MaterialAlertDialogBuilder(requireContext())
            .setCancelable(false)
            .setView(R.layout.loading_dialog)
            .create()
    }
    private val workInfo by lazy {
        WorkManager.getInstance(context).getWorkInfosByTagLiveData(TAG_TASK_SINC)
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.config_fragment, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        binding = ConfigFragmentBinding.bind(view)
        configuraBotao()
        buscaInformacoesDispositivo()
    }

    override fun onAttach(context: Context) {
        super.onAttach(context)
        this.context = context
    }

    private fun configuraBotao() {
        binding.btnBuscaMac.setOnClickListener {
            val dispositivoLocal = Sistema.getSetup()
            lifecycleScope.launch {
                atualizaBotaoBuscaNumeroInterno(false)
                viewModel.buscaDispositivoByNumeroInterno(dispositivoLocal?.numeroInterno).observe(viewLifecycleOwner) { response ->
                    if (response == null) {
                        atualizaBotaoBuscaNumeroInterno(true)
                        Toast.makeText(activity, "Dispositivo não cadastrado. Verifique!", Toast.LENGTH_SHORT).show()
                    } else {
                        atualizaBotaoBuscaNumeroInterno(true)
                        if (dispositivoLocal?.mac == response.mac) { Toast.makeText(activity, "O mac já está atualizado!", Toast.LENGTH_SHORT).show()
                        } else {
                            lifecycleScope.launch {
                                viewModel.atualizaDispositivo(response.mac)
                                Sistema.configuraSistema(SetupMapper().fromSetupDTOtoEntity(response))
                                Toast.makeText(activity,"Mac atualizado!", Toast.LENGTH_SHORT).show()
                            }
                        }
                    }
                }
            }
        }

        binding.btnConfigFragmentSinc.setOnClickListener {
            if (!verificaConexao(context)) {
                Toast.makeText(requireContext(), "Sem conexão com a internet!", Toast.LENGTH_SHORT).show()
            } else {
                sincronizaInformacoesBase()
            }
        }
    }

    private fun sincronizaInformacoesBase(){
        var work = ""
        TaskCreator(context).uniqueRequest(TaskFirebase::class, TAG_TASK_SINC,emptyMap())
        workInfo.observe((viewLifecycleOwner)) { worker ->
            if (worker.isNotEmpty()) {
                Log.d(TAG, "Work Sinc ${worker[0].state}")
                if (worker[0].state.isFinished) {
                    progressDialog.dismiss()
                    Toast.makeText(context, "Sincronizacão concluída", Toast.LENGTH_SHORT).show()
                    work = worker[0].state.toString()
                } else {
                    progressDialog.show()
                    //progressDialog. = "Sincronizando base de dados local..."
                    Handler(Looper.getMainLooper()).postDelayed({
                        verificaWorker(work)
                    }, 15000)
                }
            }
        }
    }

    private fun buscaInformacoesDispositivo(){
        lifecycleScope.launch {
            viewModel.buscaSetup().apply {
                binding.tvCaminhao.text = this?.veiculosId
                binding.tvDevice.text = "DEVICE: ${this?.mac}"
                binding.tvNumeroDispositivo.text = "Dispositivo: ${this?.numeroInterno}"
            }
        }
    }

    private fun verificaWorker(worker : String){
        println("Worker = $worker")
        if(worker == "ENQUEUED" || worker == "RUNNING" || worker == "BLOCKED" || worker == ""){
            progressDialog.dismiss()
            Toast.makeText(context, "Tempo limite excedido - Falha na sincronização", Toast.LENGTH_SHORT).show()
        }
    }

    private fun atualizaBotaoBuscaNumeroInterno(status : Boolean){
        lifecycleScope.launch {
            if(!status){
                binding.tvBtnBuscaMac.text = "Procurando dispositivo"
                binding.btnBuscaMac.isEnabled = false
            }else{
                binding.tvBtnBuscaMac.text = "Buscar novo mac"
                binding.btnBuscaMac.isEnabled = true
            }
        }
    }
}