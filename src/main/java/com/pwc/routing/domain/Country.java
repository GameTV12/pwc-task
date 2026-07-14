package com.pwc.routing.domain;

import java.util.List;

/**
 * A country as far as routing is concerned: its canonical {@code cca3} code
 * and the {@code cca3} codes of the countries it shares a land border with
 * (empty for island nations).
 */
public record Country(String cca3, List<String> borders) {
}
