package br.edu.aula.cidadeemfoco;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

public class DatabaseHelper extends SQLiteOpenHelper {

    private static final String NOME_BANCO = "cidade_em_foco.db";
    private static final int VERSAO_BANCO = 1;

    public static final String TABELA_OCORRENCIAS = "ocorrencias";
    public static final String TABELA_MIDIAS = "midias";

    public DatabaseHelper(Context context) {
        super(context, NOME_BANCO, null, VERSAO_BANCO);
    }

    @Override
    public void onConfigure(SQLiteDatabase db) {
        super.onConfigure(db);

        db.setForeignKeyConstraintsEnabled(true);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {

        String sqlOcorrencias =
                "CREATE TABLE " + TABELA_OCORRENCIAS + " (" +
                        "id INTEGER PRIMARY KEY AUTOINCREMENT, " +
                        "titulo TEXT NOT NULL, " +
                        "descricao TEXT, " +
                        "categoria TEXT NOT NULL, " +
                        "latitude REAL NOT NULL, " +
                        "longitude REAL NOT NULL, " +
                        "data_criacao TEXT, " +
                        "status TEXT NOT NULL DEFAULT 'ABERTA'" +
                        ")";

        db.execSQL(sqlOcorrencias);

        String sqlMidias =
                "CREATE TABLE " + TABELA_MIDIAS + " (" +
                        "id INTEGER PRIMARY KEY AUTOINCREMENT, " +
                        "ocorrencia_id INTEGER NOT NULL, " +
                        "tipo TEXT NOT NULL, " +
                        "caminho_arquivo TEXT NOT NULL, " +
                        "data_captura TEXT, " +
                        "FOREIGN KEY (ocorrencia_id) REFERENCES " +
                        TABELA_OCORRENCIAS + "(id) ON DELETE CASCADE" +
                        ")";

        db.execSQL(sqlMidias);
    }

    @Override
    public void onUpgrade(SQLiteDatabase db,
                          int oldVersion,
                          int newVersion) {

        // Futuras alterações na estrutura do banco
        // serão implementadas aqui.
    }
}
