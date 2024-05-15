package br.com.alura.screenmatch.service;

import java.util.List;
import java.util.stream.Collectors;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import br.com.alura.screenmatch.dto.SerieDTO;
import br.com.alura.screenmatch.model.Serie;
import br.com.alura.screenmatch.repository.SerieRepository;

@Service
public class SerieService {
    @Autowired
    private SerieRepository repositorio;

    public List<SerieDTO> obterTodasAsSeries() {
        return convertDados(repositorio.findAll());
    }

    public List<SerieDTO> obterTop5Series() {
        return convertDados(repositorio.findTop5ByOrderByAvaliacaoDesc());
    }

    private List<SerieDTO> convertDados(List<Serie> series) {
        return series.stream().map(s -> new SerieDTO(
                s.getId(),
                s.getTitulo(),
                s.getTotalTemporadas(),
                s.getAvaliacao(),
                s.getGenero(),
                s.getAtores(),
                s.getPoster(),
                s.getSinopse()))
                .collect(Collectors.toList());
    }
}