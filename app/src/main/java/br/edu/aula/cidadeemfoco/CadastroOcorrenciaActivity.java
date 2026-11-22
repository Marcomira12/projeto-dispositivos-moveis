package br.edu.aula.cidadeemfoco;

import android.Manifest;
import android.content.pm.PackageManager;
import android.location.Location;
import android.os.Bundle;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;

import com.google.android.gms.location.CurrentLocationRequest;
import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.location.Priority;
import com.google.android.gms.tasks.CancellationTokenSource;
import android.content.Intent;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;



public class CadastroOcorrenciaActivity extends AppCompatActivity {

    private EditText edtTitulo;
    private EditText edtDescricao;
    private Spinner spnCategoria;
    private TextView txtLatitude;
    private TextView txtLongitude;
    private Button btnLocalizacao;
    private Button btnSalvarOcorrencia;

    private Double latitude;
    private Double longitude;

    private boolean buscandoLocalizacao = false;
    private Button btnAdicionarMidias;
    private TextView txtStatusMidias;
    private long ocorrenciaIdSalva = -1;

    private FusedLocationProviderClient fusedLocationClient;
    private CancellationTokenSource cancellationTokenSource;

    private OcorrenciaDAO ocorrenciaDAO;

    private final ActivityResultLauncher<String[]> permissaoLocalizacao =
            registerForActivityResult(
                    new ActivityResultContracts.RequestMultiplePermissions(),
                    resultado -> {
                        if (temPermissaoLocalizacao()) {
                            capturarLocalizacao();
                        } else {
                            Toast.makeText(
                                    this,
                                    "Permissão de localização negada",
                                    Toast.LENGTH_LONG
                            ).show();
                        }
                    }
            );

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_cadastro_ocorrencia);
        Button btnConcluirCadastro =
                findViewById(R.id.btnConcluirCadastro);

        btnConcluirCadastro.setOnClickListener(v -> {
            setResult(RESULT_OK);
            finish();
        });
        inicializarComponentes();
        btnAdicionarMidias.setOnClickListener(v -> abrirCamera());
        fusedLocationClient =
                LocationServices.getFusedLocationProviderClient(this);

        ocorrenciaDAO = new OcorrenciaDAO(this);

        btnLocalizacao.setOnClickListener(v ->
                verificarPermissaoLocalizacao()
        );

        btnSalvarOcorrencia.setOnClickListener(v ->
                salvarOcorrencia()
        );
    }

    private void inicializarComponentes() {

        edtTitulo = findViewById(R.id.edtTitulo);
        edtDescricao = findViewById(R.id.edtDescricao);
        spnCategoria = findViewById(R.id.spnCategoria);

        txtLatitude = findViewById(R.id.txtLatitude);
        txtLongitude = findViewById(R.id.txtLongitude);
        btnAdicionarMidias =
                findViewById(R.id.btnAdicionarMidias);

        txtStatusMidias =
                findViewById(R.id.txtStatusMidias);

        btnAdicionarMidias.setEnabled(false);
        btnLocalizacao = findViewById(R.id.btnLocalizacao);
        btnSalvarOcorrencia =
                findViewById(R.id.btnSalvarOcorrencia);
    }

    private boolean temPermissaoLocalizacao() {

        boolean permissaoPrecisa =
                ContextCompat.checkSelfPermission(
                        this,
                        Manifest.permission.ACCESS_FINE_LOCATION
                ) == PackageManager.PERMISSION_GRANTED;

        boolean permissaoAproximada =
                ContextCompat.checkSelfPermission(
                        this,
                        Manifest.permission.ACCESS_COARSE_LOCATION
                ) == PackageManager.PERMISSION_GRANTED;

        return permissaoPrecisa || permissaoAproximada;
    }
    private void abrirCamera() {

        if (ocorrenciaIdSalva <= 0) {
            Toast.makeText(
                    this,
                    "Salve a ocorrência primeiro",
                    Toast.LENGTH_SHORT
            ).show();
            return;
        }

        Intent intent = new Intent(
                CadastroOcorrenciaActivity.this,
                CameraOcorrenciaActivity.class
        );

        intent.putExtra("ocorrencia_id", ocorrenciaIdSalva);

        startActivity(intent);
    }
    private void verificarPermissaoLocalizacao() {

        if (temPermissaoLocalizacao()) {
            capturarLocalizacao();
        } else {
            permissaoLocalizacao.launch(new String[]{
                    Manifest.permission.ACCESS_FINE_LOCATION,
                    Manifest.permission.ACCESS_COARSE_LOCATION
            });
        }
    }

    private void capturarLocalizacao() {

        if (!temPermissaoLocalizacao()) {
            verificarPermissaoLocalizacao();
            return;
        }

        if (buscandoLocalizacao) {
            return;
        }

        buscandoLocalizacao = true;
        btnLocalizacao.setEnabled(false);

        // Impede salvar coordenadas antigas
        latitude = null;
        longitude = null;

        txtLatitude.setText("Latitude: buscando...");
        txtLongitude.setText("Longitude: buscando...");

        cancellationTokenSource = new CancellationTokenSource();

        CurrentLocationRequest request =
                new CurrentLocationRequest.Builder()
                        .setPriority(Priority.PRIORITY_HIGH_ACCURACY)
                        .setMaxUpdateAgeMillis(0)
                        .setDurationMillis(15000)
                        .build();

        try {
            fusedLocationClient.getCurrentLocation(
                    request,
                    cancellationTokenSource.getToken()
            ).addOnSuccessListener(this, localizacao -> {

                finalizarBuscaLocalizacao();

                if (localizacao != null) {
                    atualizarCoordenadas(localizacao);
                } else {
                    mostrarErroLocalizacao();
                }

            }).addOnFailureListener(this, erro -> {

                finalizarBuscaLocalizacao();
                mostrarErroLocalizacao();
            });

        } catch (SecurityException e) {

            finalizarBuscaLocalizacao();
            mostrarErroLocalizacao();
        }
    }

    private void atualizarCoordenadas(@NonNull Location localizacao) {

        latitude = localizacao.getLatitude();
        longitude = localizacao.getLongitude();

        txtLatitude.setText(String.format(
                Locale.getDefault(),
                "Latitude: %.6f",
                latitude
        ));

        txtLongitude.setText(String.format(
                Locale.getDefault(),
                "Longitude: %.6f",
                longitude
        ));
    }

    private void finalizarBuscaLocalizacao() {
        buscandoLocalizacao = false;
        btnLocalizacao.setEnabled(true);
        cancellationTokenSource = null;
    }

    private void mostrarErroLocalizacao() {

        txtLatitude.setText("Latitude: não capturada");
        txtLongitude.setText("Longitude: não capturada");

        Toast.makeText(
                this,
                "Não foi possível obter a localização. " +
                        "Verifique se o GPS está ativado.",
                Toast.LENGTH_LONG
        ).show();
    }

    private void salvarOcorrencia() {

        String titulo = edtTitulo.getText().toString().trim();
        String descricao =
                edtDescricao.getText().toString().trim();

        int posicaoCategoria =
                spnCategoria.getSelectedItemPosition();

        if (titulo.isEmpty()) {
            edtTitulo.setError("Informe o título");
            edtTitulo.requestFocus();
            return;
        }

        if (posicaoCategoria == 0) {
            Toast.makeText(
                    this,
                    "Selecione uma categoria",
                    Toast.LENGTH_SHORT
            ).show();
            return;
        }

        if (latitude == null || longitude == null) {
            Toast.makeText(
                    this,
                    "Capture a localização antes de salvar",
                    Toast.LENGTH_SHORT
            ).show();
            return;
        }

        String categoria =
                spnCategoria.getSelectedItem().toString();

        String dataCriacao =
                new SimpleDateFormat(
                        "yyyy-MM-dd HH:mm:ss",
                        Locale.getDefault()
                ).format(new Date());

        Ocorrencia ocorrencia = new Ocorrencia(
                titulo,
                descricao,
                categoria,
                latitude,
                longitude,
                dataCriacao,
                "ABERTA"
        );

        try {

            long id = ocorrenciaDAO.inserir(ocorrencia);

            if (id != -1) {

                ocorrenciaIdSalva = id;

                btnAdicionarMidias.setEnabled(true);
                btnSalvarOcorrencia.setEnabled(false);

                edtTitulo.setEnabled(false);
                edtDescricao.setEnabled(false);
                spnCategoria.setEnabled(false);
                btnLocalizacao.setEnabled(false);

                txtStatusMidias.setText(
                        "Ocorrência salva! Agora você pode adicionar fotos e vídeos."
                );

                Toast.makeText(
                        this,
                        "Ocorrência salva com sucesso!",
                        Toast.LENGTH_SHORT
                ).show();

                abrirCamera();
            }else {

                Toast.makeText(
                        this,
                        "Erro ao salvar ocorrência",
                        Toast.LENGTH_LONG
                ).show();
            }

        } catch (Exception e) {

            Toast.makeText(
                    this,
                    "Erro no banco de dados: " + e.getMessage(),
                    Toast.LENGTH_LONG
            ).show();
        }
    }

    @Override
    protected void onStop() {
        super.onStop();

        if (cancellationTokenSource != null) {
            cancellationTokenSource.cancel();
            cancellationTokenSource = null;

            buscandoLocalizacao = false;
            btnLocalizacao.setEnabled(true);

            if (latitude == null || longitude == null) {
                txtLatitude.setText("Latitude: não capturada");
                txtLongitude.setText("Longitude: não capturada");
            }
        }
    }
}