package br.edu.aula.cidadeemfoco;

public class Midia {

    private long id;
    private long ocorrenciaId;
    private String tipo;
    private String caminhoArquivo;
    private String dataCaptura;

    public Midia(long id, long ocorrenciaId,
                 String tipo, String caminhoArquivo,
                 String dataCaptura) {
        this.id = id;
        this.ocorrenciaId = ocorrenciaId;
        this.tipo = tipo;
        this.caminhoArquivo = caminhoArquivo;
        this.dataCaptura = dataCaptura;
    }

    public long getId() {
        return id;
    }

    public long getOcorrenciaId() {
        return ocorrenciaId;
    }

    public String getTipo() {
        return tipo;
    }

    public String getCaminhoArquivo() {
        return caminhoArquivo;
    }

    public String getDataCaptura() {
        return dataCaptura;
    }
}