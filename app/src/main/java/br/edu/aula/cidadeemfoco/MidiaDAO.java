package br.edu.aula.cidadeemfoco;

import android.content.ContentValues;
import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.Cursor;
import java.util.ArrayList;
import java.util.List;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

public class MidiaDAO {

    private final DatabaseHelper databaseHelper;

    public MidiaDAO(Context context) {
        databaseHelper = new DatabaseHelper(context);
    }

    public long inserir(
            long ocorrenciaId,
            String tipo,
            String caminhoArquivo) {

        ContentValues valores = new ContentValues();

        valores.put("ocorrencia_id", ocorrenciaId);
        valores.put("tipo", tipo);
        valores.put("caminho_arquivo", caminhoArquivo);

        String data = new SimpleDateFormat(
                "yyyy-MM-dd HH:mm:ss",
                Locale.getDefault()
        ).format(new Date());

        valores.put("data_captura", data);

        SQLiteDatabase db =
                databaseHelper.getWritableDatabase();

        return db.insert(
                DatabaseHelper.TABELA_MIDIAS,
                null,
                valores
        );
    }
    public List<Midia> listarPorOcorrencia(long ocorrenciaId) {

        List<Midia> lista = new ArrayList<>();

        SQLiteDatabase db =
                databaseHelper.getReadableDatabase();

        try (Cursor cursor = db.query(
                DatabaseHelper.TABELA_MIDIAS,
                null,
                "ocorrencia_id = ?",
                new String[]{String.valueOf(ocorrenciaId)},
                null,
                null,
                "id ASC"
        )) {

            while (cursor.moveToNext()) {

                long id = cursor.getLong(
                        cursor.getColumnIndexOrThrow("id")
                );

                long idOcorrencia = cursor.getLong(
                        cursor.getColumnIndexOrThrow("ocorrencia_id")
                );

                String tipo = cursor.getString(
                        cursor.getColumnIndexOrThrow("tipo")
                );

                String caminho = cursor.getString(
                        cursor.getColumnIndexOrThrow("caminho_arquivo")
                );

                String data = cursor.getString(
                        cursor.getColumnIndexOrThrow("data_captura")
                );

                Midia midia = new Midia(
                        id,
                        idOcorrencia,
                        tipo,
                        caminho,
                        data
                );

                lista.add(midia);
            }
        }

        return lista;
    }
}
