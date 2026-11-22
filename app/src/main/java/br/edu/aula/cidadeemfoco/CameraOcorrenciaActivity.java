package br.edu.aula.cidadeemfoco;

import android.Manifest;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.appcompat.app.AppCompatActivity;
import androidx.camera.core.CameraSelector;
import androidx.camera.core.ImageCapture;
import androidx.camera.core.ImageCaptureException;
import androidx.camera.core.Preview;
import androidx.camera.lifecycle.ProcessCameraProvider;
import androidx.camera.video.FileOutputOptions;
import androidx.camera.video.PendingRecording;
import androidx.camera.video.Recorder;
import androidx.camera.video.Recording;
import androidx.camera.video.VideoCapture;
import androidx.camera.video.VideoRecordEvent;
import androidx.camera.view.PreviewView;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import com.google.common.util.concurrent.ListenableFuture;

import java.io.File;


public class CameraOcorrenciaActivity
        extends AppCompatActivity {

    private PreviewView previewCamera;
    private TextView txtStatusCamera;
    private Button btnTirarFoto;
    private Button btnGravarVideo;
    private Button btnFinalizarMidias;

    private ProcessCameraProvider cameraProvider;
    private ImageCapture imageCapture;
    private VideoCapture<Recorder> videoCapture;
    private Recording gravacao;

    private MidiaDAO midiaDAO;
    private long ocorrenciaId;

    private boolean modoVideo = false;
    private boolean cameraPronta = false;

    private File arquivoVideo;

    private final ActivityResultLauncher<String[]> solicitarPermissoes =
            registerForActivityResult(
                    new ActivityResultContracts.RequestMultiplePermissions(),
                    resultado -> {

                        if (temPermissao(Manifest.permission.CAMERA)) {
                            iniciarCamera();
                        } else {
                            Toast.makeText(
                                    this,
                                    "Permissão de câmera necessária",
                                    Toast.LENGTH_LONG
                            ).show();
                        }
                    }
            );

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_camera_ocorrencia);
        ViewCompat.setOnApplyWindowInsetsListener(
                findViewById(android.R.id.content),
                (view, insets) -> {

                    Insets barras = insets.getInsets(
                            WindowInsetsCompat.Type.systemBars()
                    );

                    view.setPadding(
                            barras.left,
                            barras.top,
                            barras.right,
                            barras.bottom
                    );

                    return insets;
                }
        );
        ocorrenciaId = getIntent().getLongExtra(
                "ocorrencia_id", -1
        );

        if (ocorrenciaId <= 0) {
            Toast.makeText(
                    this,
                    "Ocorrência inválida",
                    Toast.LENGTH_SHORT
            ).show();
            finish();
            return;
        }

        midiaDAO = new MidiaDAO(this);

        previewCamera = findViewById(R.id.previewCamera);
        txtStatusCamera = findViewById(R.id.txtStatusCamera);
        btnTirarFoto = findViewById(R.id.btnTirarFoto);
        btnGravarVideo = findViewById(R.id.btnGravarVideo);
        btnFinalizarMidias =
                findViewById(R.id.btnFinalizarMidias);

        btnTirarFoto.setOnClickListener(v -> {

            if (!modoVideo) {
                tirarFoto();
            } else if (gravacao == null) {
                modoVideo = false;
                configurarCamera();
            }
        });

        btnGravarVideo.setOnClickListener(v -> {

            if (!modoVideo) {
                modoVideo = true;
                configurarCamera();
            } else if (gravacao == null) {
                iniciarGravacao();
            } else {
                pararGravacao();
            }
        });

        btnFinalizarMidias.setOnClickListener(v -> finish());

        verificarPermissoes();
    }

    private boolean temPermissaoCamera() {
        return ContextCompat.checkSelfPermission(
                this,
                Manifest.permission.CAMERA
        ) == PackageManager.PERMISSION_GRANTED;
    }

    private boolean temPermissaoAudio() {
        return ContextCompat.checkSelfPermission(
                this,
                Manifest.permission.RECORD_AUDIO
        ) == PackageManager.PERMISSION_GRANTED;
    }

    private void iniciarCamera() {

        ListenableFuture<ProcessCameraProvider> future =
                ProcessCameraProvider.getInstance(this);

        future.addListener(() -> {
            try {
                cameraProvider = future.get();
                configurarCamera();

            } catch (Exception e) {
                txtStatusCamera.setText(
                        "Erro ao iniciar câmera: " +
                                e.getMessage()
                );
            }
        }, ContextCompat.getMainExecutor(this));
    }

    private void configurarCamera() {

        cameraPronta = false;

        if (cameraProvider == null) {
            return;
        }

        if (gravacao != null) {
            return;
        }

        try {
            cameraProvider.unbindAll();

            Preview preview = new Preview.Builder().build();

            preview.setSurfaceProvider(
                    previewCamera.getSurfaceProvider()
            );

            CameraSelector seletor =
                    CameraSelector.DEFAULT_BACK_CAMERA;

            if (modoVideo) {

                Recorder recorder =
                        new Recorder.Builder().build();

                videoCapture =
                        VideoCapture.withOutput(recorder);

                imageCapture = null;

                cameraProvider.bindToLifecycle(
                        this,
                        seletor,
                        preview,
                        videoCapture
                );

                btnGravarVideo.setText("Iniciar gravação");
                btnTirarFoto.setText("Modo foto");

                txtStatusCamera.setText(
                        "Modo vídeo: toque para gravar"
                );

            } else {

                imageCapture =
                        new ImageCapture.Builder().build();

                videoCapture = null;

                cameraProvider.bindToLifecycle(
                        this,
                        seletor,
                        preview,
                        imageCapture
                );

                btnTirarFoto.setText("Tirar foto");
                btnGravarVideo.setText("Modo vídeo");

                txtStatusCamera.setText(
                        "Modo foto: câmera pronta"
                );
            }

            cameraPronta = true;

        } catch (Exception e) {
            txtStatusCamera.setText(
                    "Erro ao configurar câmera: " +
                            e.getMessage()
            );
        }
    }

    private File criarArquivo(String pasta, String extensao) {

        File diretorio = new File(getFilesDir(), pasta);

        if (!diretorio.exists() && !diretorio.mkdirs()) {
            throw new IllegalStateException(
                    "Não foi possível criar a pasta"
            );
        }

        String nome = System.currentTimeMillis()
                + "_" + System.nanoTime()
                + extensao;

        return new File(diretorio, nome);
    }

    private void salvarMidia(File arquivo, String tipo) {

        long id = midiaDAO.inserir(
                ocorrenciaId,
                tipo,
                arquivo.getAbsolutePath()
        );

        if (id != -1) {
            txtStatusCamera.setText(
                    tipo + " salva com sucesso!"
            );
        } else {
            txtStatusCamera.setText(
                    "Arquivo capturado, mas não registrado"
            );
        }
    }

    private void tirarFoto() {

        if (!cameraPronta || imageCapture == null) {
            return;
        }

        File arquivo;

        try {
            arquivo = criarArquivo("fotos", ".jpg");
        } catch (Exception e) {
            txtStatusCamera.setText(e.getMessage());
            return;
        }

        ImageCapture.OutputFileOptions opcoes =
                new ImageCapture.OutputFileOptions.Builder(
                        arquivo
                ).build();

        imageCapture.takePicture(
                opcoes,
                ContextCompat.getMainExecutor(this),
                new ImageCapture.OnImageSavedCallback() {

                    @Override
                    public void onImageSaved(
                            ImageCapture.OutputFileResults resultado) {

                        salvarMidia(arquivo, "FOTO");
                    }

                    @Override
                    public void onError(
                            ImageCaptureException erro) {

                        txtStatusCamera.setText(
                                "Erro na foto: " +
                                        erro.getMessage()
                        );
                    }
                }
        );
    }

    private void iniciarGravacao() {

        if (!cameraPronta || videoCapture == null) {
            return;
        }

        try {
            arquivoVideo = criarArquivo(
                    "videos", ".mp4"
            );

            FileOutputOptions opcoes =
                    new FileOutputOptions.Builder(
                            arquivoVideo
                    ).build();

            PendingRecording pendente =
                    videoCapture.getOutput()
                            .prepareRecording(this, opcoes);

            if (temPermissao(Manifest.permission.RECORD_AUDIO)) {
                if (ActivityCompat.checkSelfPermission(this, Manifest.permission.RECORD_AUDIO) != PackageManager.PERMISSION_GRANTED) {
                    // TODO: Consider calling
                    //    ActivityCompat#requestPermissions
                    // here to request the missing permissions, and then overriding
                    //   public void onRequestPermissionsResult(int requestCode, String[] permissions,
                    //                                          int[] grantResults)
                    // to handle the case where the user grants the permission. See the documentation
                    // for ActivityCompat#requestPermissions for more details.
                    return;
                }
                pendente = pendente.withAudioEnabled();
            }

            gravacao = pendente.start(
                    ContextCompat.getMainExecutor(this),
                    evento -> {

                        if (evento instanceof
                                VideoRecordEvent.Start) {

                            txtStatusCamera.setText(
                                    "Gravando vídeo..."
                            );

                        } else if (evento instanceof
                                VideoRecordEvent.Finalize) {

                            VideoRecordEvent.Finalize fim =
                                    (VideoRecordEvent.Finalize) evento;

                            gravacao = null;
                            btnGravarVideo.setText(
                                    "Iniciar gravação"
                            );

                            btnTirarFoto.setEnabled(true);
                            btnFinalizarMidias.setEnabled(true);

                            if (!fim.hasError()) {
                                salvarMidia(
                                        arquivoVideo,
                                        "VIDEO"
                                );
                            } else {
                                txtStatusCamera.setText(
                                        "Erro ao salvar vídeo: " +
                                                fim.getError()
                                );
                            }
                        }
                    }
            );

            btnGravarVideo.setText("Parar gravação");
            btnTirarFoto.setEnabled(false);
            btnFinalizarMidias.setEnabled(false);

        } catch (Exception e) {

            gravacao = null;

            btnTirarFoto.setEnabled(true);
            btnFinalizarMidias.setEnabled(true);

            txtStatusCamera.setText(
                    "Erro na gravação: " + e.getMessage()
            );
        }
    }
    private void verificarPermissoes() {

        String[] permissoes = {
                Manifest.permission.CAMERA,
                Manifest.permission.RECORD_AUDIO
        };

        boolean cameraPermitida =
                temPermissao(Manifest.permission.CAMERA);

        boolean audioPermitido =
                temPermissao(Manifest.permission.RECORD_AUDIO);

        if (cameraPermitida && audioPermitido) {
            iniciarCamera();
        } else {
            solicitarPermissoes.launch(permissoes);
        }
    }
    private boolean temPermissao(String permissao) {
        return ContextCompat.checkSelfPermission(
                this,
                permissao
        ) == PackageManager.PERMISSION_GRANTED;
    }

    private void pararGravacao() {

        if (gravacao != null) {
            btnGravarVideo.setEnabled(false);
            gravacao.stop();
            btnGravarVideo.setEnabled(true);
        }
    }

    @Override
    protected void onDestroy() {

        if (gravacao != null) {
            gravacao.stop();
            gravacao = null;
        }

        super.onDestroy();
    }
}
