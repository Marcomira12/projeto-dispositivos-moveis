package br.edu.aula.cidadeemfoco;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.MediaController;
import android.widget.TextView;
import android.widget.Toast;
import android.widget.VideoView;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.core.graphics.Insets;

import java.io.File;
import java.util.List;
import java.util.Locale;
public class DetalhesOcorrenciaActivity
        extends AppCompatActivity {

    private LinearLayout containerMidias;
    private OcorrenciaDAO ocorrenciaDAO;
    private MidiaDAO midiaDAO;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_detalhes_ocorrencia);

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

        ocorrenciaDAO = new OcorrenciaDAO(this);
        midiaDAO = new MidiaDAO(this);

        containerMidias = findViewById(R.id.containerMidias);

        Button btnVoltar =
                findViewById(R.id.btnVoltarDetalhes);

        btnVoltar.setOnClickListener(v -> finish());

        long ocorrenciaId = getIntent().getLongExtra(
                "ocorrencia_id", -1
        );

        if (ocorrenciaId <= 0) {
            Toast.makeText(
                    this,
                    "ID da ocorrência inválido",
                    Toast.LENGTH_LONG
            ).show();
            finish();
            return;
        }

        carregarDetalhes(ocorrenciaId);
    }

    private void carregarDetalhes(long ocorrenciaId) {

        Ocorrencia ocorrencia =
                ocorrenciaDAO.buscarPorId(ocorrenciaId);

        if (ocorrencia == null) {
            Toast.makeText(
                    this,
                    "Ocorrência não encontrada",
                    Toast.LENGTH_LONG
            ).show();
            finish();
            return;
        }

        TextView titulo =
                findViewById(R.id.txtTituloDetalhes);

        TextView categoria =
                findViewById(R.id.txtCategoriaDetalhes);

        TextView descricao =
                findViewById(R.id.txtDescricaoDetalhes);

        TextView localizacao =
                findViewById(R.id.txtLocalizacaoDetalhes);

        TextView data =
                findViewById(R.id.txtDataDetalhes);

        TextView status =
                findViewById(R.id.txtStatusDetalhes);

        titulo.setText(ocorrencia.getTitulo());

        categoria.setText(
                "Categoria: " + ocorrencia.getCategoria()
        );

        descricao.setText(
                "Descrição: " + ocorrencia.getDescricao()
        );

        localizacao.setText(String.format(
                Locale.getDefault(),
                "Latitude: %.6f\nLongitude: %.6f",
                ocorrencia.getLatitude(),
                ocorrencia.getLongitude()
        ));

        data.setText(
                "Data: " + ocorrencia.getDataCriacao()
        );

        status.setText(
                "Status: " + ocorrencia.getStatus()
        );

        carregarMidias(ocorrenciaId);
    }

    private void carregarMidias(long ocorrenciaId) {

        containerMidias.removeAllViews();

        List<Midia> lista =
                midiaDAO.listarPorOcorrencia(ocorrenciaId);

        if (lista.isEmpty()) {
            adicionarTexto("Nenhuma mídia cadastrada.");
            return;
        }

        for (Midia midia : lista) {

            File arquivo = new File(
                    midia.getCaminhoArquivo()
            );

            if (!arquivo.isFile()) {
                adicionarTexto(
                        "Arquivo não encontrado: " +
                                arquivo.getName()
                );
                continue;
            }

            if ("FOTO".equalsIgnoreCase(midia.getTipo())) {
                mostrarFoto(arquivo);
            } else if ("VIDEO".equalsIgnoreCase(midia.getTipo())) {
                mostrarVideo(arquivo);
            } else {
                adicionarTexto(
                        "Tipo de mídia não reconhecido"
                );
            }
        }
    }

    private void mostrarFoto(File arquivo) {

        BitmapFactory.Options opcoes =
                new BitmapFactory.Options();

        opcoes.inJustDecodeBounds = true;

        BitmapFactory.decodeFile(
                arquivo.getAbsolutePath(), opcoes
        );

        int escala = 1;

        while (opcoes.outWidth / escala > 1080
                || opcoes.outHeight / escala > 1080) {
            escala *= 2;
        }

        opcoes.inJustDecodeBounds = false;
        opcoes.inSampleSize = escala;

        Bitmap imagem = BitmapFactory.decodeFile(
                arquivo.getAbsolutePath(), opcoes
        );

        if (imagem == null) {
            adicionarTexto("Não foi possível abrir a foto.");
            return;
        }

        ImageView imageView = new ImageView(this);

        LinearLayout.LayoutParams parametros =
                new LinearLayout.LayoutParams(
                        ViewGroup.LayoutParams.MATCH_PARENT,
                        dp(240)
                );

        parametros.bottomMargin = dp(12);

        imageView.setLayoutParams(parametros);
        imageView.setScaleType(
                ImageView.ScaleType.FIT_CENTER
        );

        imageView.setImageBitmap(imagem);

        containerMidias.addView(imageView);
    }

    private void mostrarVideo(File arquivo) {

        TextView identificacao = new TextView(this);
        identificacao.setText(
                "Vídeo: " + arquivo.getName()
        );
        identificacao.setPadding(0, dp(8), 0, dp(8));

        containerMidias.addView(identificacao);

        VideoView videoView = new VideoView(this);

        LinearLayout.LayoutParams parametros =
                new LinearLayout.LayoutParams(
                        ViewGroup.LayoutParams.MATCH_PARENT,
                        dp(240)
                );

        videoView.setLayoutParams(parametros);

        MediaController controles =
                new MediaController(this);

        controles.setAnchorView(videoView);

        videoView.setMediaController(controles);
        videoView.setVideoPath(arquivo.getAbsolutePath());

        containerMidias.addView(videoView);

        Button reproduzir = new Button(this);
        reproduzir.setText("Reproduzir vídeo");

        reproduzir.setOnClickListener(v -> {
            videoView.start();
            controles.show();
        });

        containerMidias.addView(reproduzir);
    }

    private void adicionarTexto(String mensagem) {

        TextView texto = new TextView(this);

        texto.setText(mensagem);
        texto.setPadding(0, dp(8), 0, dp(8));

        containerMidias.addView(texto);
    }

    private int dp(int valor) {
        return (int) (
                valor * getResources()
                        .getDisplayMetrics().density
        );
    }

    @Override
    protected void onPause() {
        super.onPause();

        for (int i = 0; i < containerMidias.getChildCount(); i++) {
            if (containerMidias.getChildAt(i) instanceof VideoView) {
                ((VideoView) containerMidias.getChildAt(i)).pause();
            }
        }
    }
}
