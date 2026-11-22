package br.edu.aula.cidadeemfoco;

public class Ocorrencia {

    private long id;
    private String titulo;
    private String descricao;
    private String categoria;
    private double latitude;
    private double longitude;
    private String dataCriacao;
    private String status;

    // Construtor vazio
    public Ocorrencia() {
    }

    // Construtor para cadastrar novas ocorrências
    public Ocorrencia(String titulo, String descricao,
                      String categoria, double latitude,
                      double longitude, String dataCriacao,
                      String status) {

        this.titulo = titulo;
        this.descricao = descricao;
        this.categoria = categoria;
        this.latitude = latitude;
        this.longitude = longitude;
        this.dataCriacao = dataCriacao;
        this.status = status;
    }

    // Getters e Setters

    public long getId() {
        return id;
    }

    public void setId(long id) {
        this.id = id;
    }

    public String getTitulo() {
        return titulo;
    }

    public void setTitulo(String titulo) {
        this.titulo = titulo;
    }

    public String getDescricao() {
        return descricao;
    }

    public void setDescricao(String descricao) {
        this.descricao = descricao;
    }

    public String getCategoria() {
        return categoria;
    }

    public void setCategoria(String categoria) {
        this.categoria = categoria;
    }

    public double getLatitude() {
        return latitude;
    }

    public void setLatitude(double latitude) {
        this.latitude = latitude;
    }

    public double getLongitude() {
        return longitude;
    }

    public void setLongitude(double longitude) {
        this.longitude = longitude;
    }

    public String getDataCriacao() {
        return dataCriacao;
    }

    public void setDataCriacao(String dataCriacao) {
        this.dataCriacao = dataCriacao;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }
}