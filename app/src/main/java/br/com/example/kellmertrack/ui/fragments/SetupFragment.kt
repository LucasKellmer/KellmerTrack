package br.com.example.kellmertrack.ui.fragments

import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.fragment.app.viewModels
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import br.com.example.kellmertrack.R
import br.com.example.kellmertrack.databinding.SetupFragmentBinding
import br.com.example.kellmertrack.extensions.verificaConexao
import br.com.example.kellmertrack.local.model.mappers.SetupMapper
import br.com.example.kellmertrack.remote.model.Status
import br.com.example.kellmertrack.ui.MainActivity
import br.com.example.kellmertrack.ui.viewmodel.AppViewModel
import br.com.example.kellmertrack.ui.viewmodel.ComponentesFragments
import br.com.example.kellmertrack.ui.viewmodel.SetupViewModel
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

@AndroidEntryPoint
class SetupFragment : Fragment(){
    private lateinit var binding: SetupFragmentBinding
    private val viewModel by viewModels<SetupViewModel>()
    private val routerController by lazy { findNavController() }
    private val appViewModel: AppViewModel by activityViewModels()
    private val progressDialog by lazy {
        MaterialAlertDialogBuilder(requireContext())
            .setCancelable(false)
            .setView(R.layout.loading_dialog)
            .create()
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.setup_fragment, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        val activity = requireActivity() as MainActivity
        activity.checkBasePermissions()
        binding = SetupFragmentBinding.bind(view)
        //binding.edt1.addTextChangedListener(this)
        //binding.edt2.addTextChangedListener(this)
        //binding.edt3.addTextChangedListener(this)
        configuraBotao()
    }

    override fun onResume() {
        super.onResume()
        appViewModel.setBottomBar(ComponentesFragments(bottomBar = false))
    }

    private fun configuraBotao(){
        binding.btnBuscarSetup.setOnClickListener {
            if(verificaConexao(context)){
                //val numeroDispositivo = "${binding.edt1.text}${binding.edt2.text}${binding.edt3.text}"
                configurarDispositivo(binding.edtNumeroDispositivo.text.toString())
            }else
                Toast.makeText(activity, "Verifique a conexão com a internet!", Toast.LENGTH_SHORT).show()
        }
    }

    private fun configurarDispositivo(dispositivo: String) {
        progressDialog.show()
        lifecycleScope.launch(Dispatchers.Main) {
            viewModel.buscaDispositivo(dispositivo).observe(viewLifecycleOwner) { response ->
                when (response.status) {
                    Status.SUCCESS -> {
                        if (response.data == null){
                            progressDialog.dismiss()
                            Toast.makeText(activity, "Dispositivo não cadastrado. Verifique!", Toast.LENGTH_SHORT).show()
                        } else if (response.data.dataVinculo != null) {
                            progressDialog.dismiss()
                            Toast.makeText(activity, "Dispositivo com vínculo ativo no sistema. Verifique!", Toast.LENGTH_SHORT).show()
                        }
                        else {
                            lifecycleScope.launch{
                                viewModel.salvaDispositivo(SetupMapper().fromSetupDTOtoEntity(response.data))
                                progressDialog.dismiss()
                                routerController.navigate(R.id.action_SetupFragment_to_HomeFragment)
                            }
                        }
                    }
                    Status.ERROR -> {
                        progressDialog.dismiss()
                        Toast.makeText(activity, "Erro: ${response.message}", Toast.LENGTH_SHORT).show()
                    }
                    else -> {}
                }
            }
        }
    }

    /*override fun beforeTextChanged(p0: CharSequence?, p1: Int, p2: Int, p3: Int) {

    }

    override fun onTextChanged(p0: CharSequence?, p1: Int, p2: Int, p3: Int) {

    }

    override fun afterTextChanged(p: Editable?) {
        if(p?.length == 1){
            if (binding.edt1.length() == 1){
                binding.edt2.requestFocus()
                if (binding.edt2.length() == 1){
                    binding.edt3.requestFocus()
                }
            }
        }else if(p?.length == 0){
            if(binding.edt3.length() == 0){
                binding.edt2.requestFocus()
            }
            if(binding.edt2.length() == 0){
                binding.edt1.requestFocus()
            }
        }
    }*/
}