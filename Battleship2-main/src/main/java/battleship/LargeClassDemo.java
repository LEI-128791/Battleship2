// 128782 - João Mota
// Classe de teste do quality gate Qodana (passo 9 da Tarefa 2A). NAO integrar no main.
// Versao CORRIGIDA: as cadeias de 10 ifs foram substituidas por tabelas (arrays/Map),
// com o mesmo comportamento. Complexidade ciclomatica total: 55 -> cerca de 10 (< limite 46).
package battleship;

import java.util.Map;

public class LargeClassDemo {

    private static final String[] SHIP_NAMES = {
        "Barca", "Caravela", "Nau", "Fragata", "Galeao",
        "Submarino", "Corveta", "Patrulha", "Couracado", "Porta-avioes"
    };

    private static final Map<Character, String> COMPASS_NAMES = Map.of(
        'N', "Norte", 'S', "Sul", 'E', "Este", 'O', "Oeste", 'U', "Desconhecido"
    );

    private static final String[] COLORS = {
        "Azul", "Vermelho", "Verde", "Amarelo", "Preto",
        "Branco", "Cinzento", "Laranja", "Roxo", "Rosa"
    };

    private static final String[] MESSAGES = {
        "Agua", "Tiro", "Afundado", "Repetido", "Invalido",
        "Vitoria", "Derrota", "Empate", "Pausa", "Fim"
    };

    public String shipName(int size) {
        return lookup(SHIP_NAMES, size - 1, "Desconhecido");
    }

    public String compassName(char c) {
        return COMPASS_NAMES.getOrDefault(Character.toUpperCase(c), "Invalido");
    }

    public String scoreLabel(int hits) {
        return (hits >= 0 && hits <= 9) ? hits + " acertos" : "Muitos acertos";
    }

    public String colorFor(int code) {
        return lookup(COLORS, code, "Sem cor");
    }

    public String messageFor(int code) {
        return lookup(MESSAGES, code, "Sem mensagem");
    }

    private static String lookup(String[] table, int index, String fallback) {
        return (index >= 0 && index < table.length) ? table[index] : fallback;
    }
}
