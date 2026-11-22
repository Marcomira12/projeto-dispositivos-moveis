package br.edu.aula.cidadeemfoco;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;

import java.util.ArrayList;
import java.util.List;


public class OcorrenciaDAO {

    private final DatabaseHelper databaseHelper;

    public OcorrenciaDAO(Context context) {
        databaseHelper = new DatabaseHelper(context);
    }

    // Salva uma nova ocorrência no SQLite
    public long inserir(Ocorrencia ocorrencia) {

        SQLiteDatabase db = databaseHelper.getWritableDatabase();

        ContentValues valores = new ContentValues();

        valores.put("titulo", ocorrencia.getTitulo());
        valores.put("descricao", ocorrencia.getDescricao());
        valores.put("categoria", ocorrencia.getCategoria());
        valores.put("latitude", ocorrencia.getLatitude());
        valores.put("longitude", ocorrencia.getLongitude());
        valores.put("data_criacao", ocorrencia.getDataCriacao());
        valores.put("status", ocorrencia.getStatus());

        return db.insert(
                DatabaseHelper.TABELA_OCORRENCIAS,
                null,
                valores
        );
    }

    // Recupera todas as ocorrências cadastradas
    public List<Ocorrencia> listarTodas() {

        List<Ocorrencia> lista = new ArrayList<>();

        SQLiteDatabase db = databaseHelper.getReadableDatabase();

        try (Cursor cursor = db.query(
                DatabaseHelper.TABELA_OCORRENCIAS,
                null,
                null,
                null,
                null,
                null,
                "id DESC"
        )) {

            while (cursor.moveToNext()) {
                lista.add(converterCursor(cursor));
            }
        }

        return lista;
    }

    // Busca uma ocorrência pelo ID
    public Ocorrencia buscarPorId(long id) {

        SQLiteDatabase db = databaseHelper.getReadableDatabase();

        try (Cursor cursor = db.query(
                DatabaseHelper.TABELA_OCORRENCIAS,
                null,
                "id = ?",
                new String[]{String.valueOf(id)},
                null,
                null,
                null
        )) {

            if (cursor.moveToFirst()) {
                return converterCursor(cursor);
            }
        }

        return null;
    }

    // Converte uma linha do SQLite em objeto Ocorrencia
    private Ocorrencia converterCursor(Cursor cursor) {

        Ocorrencia ocorrencia = new Ocorrencia();

        ocorrencia.setId(cursor.getLong(
                cursor.getColumnIndexOrThrow("id")));

        ocorrencia.setTitulo(cursor.getString(
                cursor.getColumnIndexOrThrow("titulo")));

        ocorrencia.setDescricao(cursor.getString(
                cursor.getColumnIndexOrThrow("descricao")));

        ocorrencia.setCategoria(cursor.getString(
                cursor.getColumnIndexOrThrow("categoria")));

        ocorrencia.setLatitude(cursor.getDouble(
                cursor.getColumnIndexOrThrow("latitude")));

        ocorrencia.setLongitude(cursor.getDouble(
                cursor.getColumnIndexOrThrow("longitude")));

        ocorrencia.setDataCriacao(cursor.getString(
                cursor.getColumnIndexOrThrow("data_criacao")));

        ocorrencia.setStatus(cursor.getString(
                cursor.getColumnIndexOrThrow("status")));

        return ocorrencia;
    }
}