package it.carcatalog.dto;

import org.springframework.data.domain.Page;

import java.util.List;
import java.util.function.Function;

/** Wrapper stabile per la paginazione (non serializziamo direttamente Page di Spring). */
public record PaginaResponse<T>(List<T> contenuto, int pagina, int dimensione, long totaleElementi, int totalePagine) {
    public static <E, T> PaginaResponse<T> from(Page<E> page, Function<E, T> mapper) {
        return new PaginaResponse<>(page.getContent().stream().map(mapper).toList(),
                page.getNumber(), page.getSize(), page.getTotalElements(), page.getTotalPages());
    }
}
