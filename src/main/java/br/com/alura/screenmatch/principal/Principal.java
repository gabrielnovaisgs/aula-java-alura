package br.com.alura.screenmatch.principal;

import br.com.alura.screenmatch.model.Categoria;
import br.com.alura.screenmatch.model.DadosSerie;
import br.com.alura.screenmatch.model.DadosTemporada;
import br.com.alura.screenmatch.model.Episodio;
import br.com.alura.screenmatch.model.Serie;
import br.com.alura.screenmatch.repository.SerieRepository;
import br.com.alura.screenmatch.service.ConsumoApi;
import br.com.alura.screenmatch.service.ConverteDados;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.DoubleSummaryStatistics;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Scanner;
import java.util.stream.Collector;
import java.util.stream.Collectors;

import org.springframework.beans.factory.annotation.Autowired;

public class Principal {

    private Scanner leitura = new Scanner(System.in);
    private ConsumoApi consumo = new ConsumoApi();
    private ConverteDados conversor = new ConverteDados();
    private List<DadosSerie> dadosSeries = new ArrayList<>();
    private final String ENDERECO = "https://www.omdbapi.com/?t=";
    private final String API_KEY = "&apikey=6585022c";
    private SerieRepository repositorio;
    private Optional<Serie> serieBusca;
    private List<Serie> series = new ArrayList<>();

    public Principal(SerieRepository repository) {
        this.repositorio = repository;
    }

    public void exibeMenu() {
        var menu = """
                1 - Buscar séries
                2 - Buscar episódios
                3 - Listar series buscadas
                4 - Buscar série por titulo
                5 - Buscar série por ator
                6 - Buscar as top 5 séries
                7 - Buscar série por categoria
                8 - Buscar série por temporada e avalicao
                9 - Buscar por nome do episódio
                10 - Buscar top episodios de uma série
                11 - Buscar episodios a partir de uma data


                0 - Sair
                """;

        var opcao = -1;
        while (opcao != 0) {

            System.out.println(menu);
            opcao = leitura.nextInt();
            leitura.nextLine();

            switch (opcao) {
                case 1:
                    buscarSerieWeb();
                    break;
                case 2:
                    buscarEpisodioPorSerie();
                    break;
                case 3:
                    listarSeriesBuscadas();
                    break;
                case 4:
                    buscarSeriePorTitulo();
                    break;
                case 5:
                    buscarSeriesPorAutor();
                    break;
                case 6:
                    buscarTop5Series();
                    break;
                case 7:
                    buscarSeriesPorCategoria();
                    break;
                case 8:
                    filtrarSeriesPorTemporadaEAvaliacao();
                    break;
                case 9:
                    buscarEpisodioPorTrecho();
                    break;
                case 10:
                    topEpisodiosPorSerie();
                    break;

                case 11:
                    buscarEpisodiosApartirDeUmaData();
                    break;
                case 0:
                    System.out.println("Saindo...");
                    break;

                default:
                    System.out.println("Opção inválida");
            }
        }
    }

    private void buscarEpisodiosApartirDeUmaData() {
        buscarSeriePorTitulo();
        if (serieBusca.isPresent()) {
            System.out.println("Digite o ano limite de lançamento");
            var anoLancamento = leitura.nextInt();

            List<Episodio> episodiosAno = repositorio.episodiosPorSerieEAno(serieBusca.get(), anoLancamento);
            episodiosAno.forEach(
                    e -> System.out.println("Episodio: " + e.getTitulo() + " - Ano: " + e.getDataLancamento()));
        }
    }

    private void topEpisodiosPorSerie() {
        buscarSeriePorTitulo();
        if (serieBusca.isPresent()) {
            Serie serie = serieBusca.get();
            List<Episodio> topEpisodios = repositorio.topEpisodiosPorSerie(serie);
            topEpisodios.forEach(
                    e -> System.out.println("Episodios: " + e.getTitulo() + " - Avaliação: " + e.getAvaliacao()));
        }
    }

    private void buscarEpisodioPorTrecho() {
        System.out.println("Qual o nome do episodio para busca?");
        var trechoEpisodio = leitura.nextLine();
        List<Episodio> episodiosEncontrados = repositorio.episodiosPorTrecho(trechoEpisodio);
        episodiosEncontrados.forEach(
                e -> System.out.println("Episodio: " + e.getTitulo() + " - Serie: " + e.getSerie().getTitulo()));
    }

    private void filtrarSeriesPorTemporadaEAvaliacao() {
        System.out.println("Até quantas temporadas?");
        var totalTemporadas = leitura.nextInt();
        System.out.println("Com avaliação a partir de que valor?");
        var avaliacao = leitura.nextDouble();

        List<Serie> filtroSeries = repositorio.seriesPorTemporadaEAvaliacao(totalTemporadas, avaliacao);

        System.out.println("*** Séries filtradas **");
        filtroSeries.forEach(s -> System.out.println(s.getTitulo() + " - avaliação " + s.getAvaliacao()));

    }

    private void buscarSeriesPorCategoria() {
        System.out.println("Bucar por categoria/genero: ");
        var nomeGenero = leitura.nextLine();

        Categoria categoria = Categoria.fromPortugues(nomeGenero);
        List<Serie> series = repositorio.findByGenero(categoria);
        System.out.println("Serires da categoria " + categoria);
        series.forEach(s -> System.out.println("Nome: " + s.getTitulo()));
    }

    private void buscarTop5Series() {
        List<Serie> seriesEncontradas = repositorio.findTop5ByOrderByAvaliacaoDesc();
        seriesEncontradas.stream().map(s -> s.getTitulo() + " - " + s.getAvaliacao()).forEach(System.out::println);
    }

    private void buscarSeriesPorAutor() {
        System.out.println("Qual o nome para buscar");
        var nomeAutor = leitura.nextLine();
        List<Serie> seriesEncontradas = repositorio.findByAtoresContainingIgnoreCase(nomeAutor);
        seriesEncontradas.stream().map(s -> s.getTitulo() + " - " + s.getAvaliacao()).forEach(System.out::println);
    }

    private void buscarSeriePorTitulo() {

        System.out.println("Digite o nome da série para busca");
        var nomeSerie = leitura.nextLine();

        this.serieBusca = repositorio.findByTituloContainingIgnoreCase(nomeSerie);

        if (serieBusca.isPresent()) {
            var serieEncontrada = serieBusca.get();
            System.out.println("Serie encontrada " + serieBusca.get());
        } else {
            System.out.println("Seria não foi encontrada");
        }

    }

    private void buscarSerieWeb() {
        DadosSerie dados = getDadosSerie();

        Serie serie = new Serie(dados);
        repositorio.save(serie);

        // dadosSeries.add(dados);
        System.out.println(dados);
    }

    private DadosSerie getDadosSerie() {
        System.out.println("Digite o nome da série para busca");
        var nomeSerie = leitura.nextLine();
        var json = consumo.obterDados(ENDERECO + nomeSerie.replace(" ", "+") + API_KEY);
        DadosSerie dados = conversor.obterDados(json, DadosSerie.class);
        return dados;
    }

    private void buscarEpisodioPorSerie() {
        listarSeriesBuscadas();
        System.out.println("Digite o nome da série para busca");
        var nomeSerie = leitura.nextLine();

        Optional<Serie> serie = repositorio.findByTituloContainingIgnoreCase(nomeSerie);

        if (serie.isPresent()) {
            var serieEncontrada = serie.get();
            List<DadosTemporada> temporadas = new ArrayList<>();

            for (int i = 1; i <= serieEncontrada.getTotalTemporadas(); i++) {
                var json = consumo
                        .obterDados(
                                ENDERECO + serieEncontrada.getTitulo().replace(" ", "+") + "&season=" + i + API_KEY);
                DadosTemporada dadosTemporada = conversor.obterDados(json, DadosTemporada.class);
                temporadas.add(dadosTemporada);
            }

            temporadas.forEach(System.out::println);
            List<Episodio> episodios = temporadas.stream()
                    .flatMap(d -> d.episodios().stream()
                            .map(e -> new Episodio(e.numero(), e)))
                    .collect(Collectors.toList());

            serieEncontrada.setEpisodios(episodios);
            repositorio.save(serieEncontrada);
        } else {
            System.out.println("Série não encontrada");
        }
    }

    private void listarSeriesBuscadas() {
        this.series = repositorio.findAll();
        /*
         * series = dadosSeries.stream()
         * .map(d -> new Serie(d))
         * .collect(Collectors.toList());
         */
        series.stream()
                .sorted(Comparator.comparing(Serie::getGenero))
                .forEach(System.out::println);
    }
}