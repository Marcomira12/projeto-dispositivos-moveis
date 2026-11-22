package br.edu.aula.cidadeemfoco;

import android.os.Bundle;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import android.content.Intent;
import android.widget.Button;
import android.widget.Toast;
import java.util.Map;
import java.util.HashMap;
import org.maplibre.android.MapLibre;
import org.maplibre.android.annotations.Marker;
import org.maplibre.android.annotations.MarkerOptions;
import org.maplibre.android.camera.CameraPosition;
import org.maplibre.android.geometry.LatLng;
import org.maplibre.android.maps.MapLibreMap;
import org.maplibre.android.maps.MapView;
import org.maplibre.android.maps.Style;

import java.util.ArrayList;
import java.util.List;

public class MainActivity extends AppCompatActivity {

    private MapView mapView;
    private final Map<Long, Long> idsOcorrencias =
            new HashMap<>();
    private MapLibreMap mapa;
    private OcorrenciaDAO ocorrenciaDAO;

    private boolean mapaCarregado = false;

    private final List<Marker> marcadores =
            new ArrayList<>();
    private static final double LATITUDE=-12.9714;
    private static final double LONGITUDE=-38.5014;
    private static final String URL="https://tiles.openfreemap.org/styles/liberty";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        MapLibre.getInstance(this);
        setContentView(R.layout.activity_main);
        ocorrenciaDAO = new OcorrenciaDAO(this);
        Button btnNovaOcorrencia =
                findViewById(R.id.btnNovaOcorrencia);

        btnNovaOcorrencia.setOnClickListener(v -> {

            Intent intent = new Intent(
                    MainActivity.this,
                    CadastroOcorrenciaActivity.class
            );

            startActivity(intent);
        });

        mapView=findViewById(R.id.mapView);
        mapView.onCreate(savedInstanceState);
        mapView.getMapAsync(map->{

            mapa=map;
            mapa.setOnInfoWindowClickListener(marcador -> {

                Long ocorrenciaId =
                        idsOcorrencias.get(marcador.getId());

                if (ocorrenciaId == null) {
                    return false;
                }

                Intent intent = new Intent(
                        MainActivity.this,
                        DetalhesOcorrenciaActivity.class
                );

                intent.putExtra("ocorrencia_id", ocorrenciaId);

                startActivity(intent);

                return true;
            });
            mapa.setStyle(new Style.Builder().fromUri(URL),
                    style -> {

                        CameraPosition posicao =
                                new CameraPosition.Builder()
                                        .target(new LatLng(
                                                LATITUDE,
                                                LONGITUDE
                                        ))
                                        .zoom(13.0)
                                        .build();

                        mapa.setCameraPosition(posicao);

                        mapa.getUiSettings()
                                .setZoomGesturesEnabled(true);

                        mapaCarregado = true;

                        carregarOcorrencias();
            });
        });

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });


    }
    private void carregarOcorrencias() {

        if (!mapaCarregado || mapa == null) {
            return;
        }

        // Remove os marcadores carregados anteriormente
        for (Marker marcador : marcadores) {
            mapa.removeMarker(marcador);
        }

        marcadores.clear();
        idsOcorrencias.clear();
        try {

            List<Ocorrencia> ocorrencias =
                    ocorrenciaDAO.listarTodas();

            int total = 0;

            for (Ocorrencia ocorrencia : ocorrencias) {

                double latitude = ocorrencia.getLatitude();
                double longitude = ocorrencia.getLongitude();

                // Evita coordenadas inválidas
                if (!Double.isFinite(latitude)
                        || !Double.isFinite(longitude)
                        || latitude < -90 || latitude > 90
                        || longitude < -180 || longitude > 180) {
                    continue;
                }

                MarkerOptions opcoes = new MarkerOptions()
                        .position(new LatLng(
                                latitude,
                                longitude
                        ))
                        .title(ocorrencia.getTitulo())
                        .snippet(ocorrencia.getDescricao());

                Marker marcador = mapa.addMarker(opcoes);

                marcadores.add(marcador);
                idsOcorrencias.put(
                        marcador.getId(),
                        ocorrencia.getId()
                );
                total++;
            }

            Toast.makeText(
                    this,
                    total + " ocorrências carregadas",
                    Toast.LENGTH_SHORT
            ).show();

        } catch (Exception e) {

            Toast.makeText(
                    this,
                    "Erro ao carregar ocorrências: "
                            + e.getMessage(),
                    Toast.LENGTH_LONG
            ).show();
        }
    }
    @Override
    protected void onStart(){
        super.onStart();
        mapView.onStart();
    }
    @Override
    protected void onResume() {
        super.onResume();

        if (mapView != null) {
            mapView.onResume();
        }

        if (mapaCarregado) {
            carregarOcorrencias();
        }
    }
    @Override
    protected void onPause(){
        mapView.onPause();
        super.onPause();
    }
    @Override
    protected void onStop(){
        mapView.onStop();
        super.onStop();
    }
    @Override
    protected void onDestroy(){
        mapView.onDestroy();
        super.onDestroy();
    }
    @Override
    public void onLowMemory(){
        super.onLowMemory();
        mapView.onLowMemory();
    }
    @Override
    protected void onSaveInstanceState(Bundle outState){
        super.onSaveInstanceState(outState);
        mapView.onSaveInstanceState(outState);
    }
}