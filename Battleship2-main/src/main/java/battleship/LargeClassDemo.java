// 128782 - João Mota
// Classe criada DE PROPOSITO para provocar o cheiro Large Class (God Class)
// e testar o quality gate Qodana (passo 9 da Tarefa 2A). NAO integrar no main.
// 5 metodos x (10 ifs + 1) = complexidade ciclomatica total 55 > limite 46.
package battleship;

public class LargeClassDemo {

    public String shipName(int size) {
        if (size == 1) {
            return "Barca";
        }
        if (size == 2) {
            return "Caravela";
        }
        if (size == 3) {
            return "Nau";
        }
        if (size == 4) {
            return "Fragata";
        }
        if (size == 5) {
            return "Galeao";
        }
        if (size == 6) {
            return "Submarino";
        }
        if (size == 7) {
            return "Corveta";
        }
        if (size == 8) {
            return "Patrulha";
        }
        if (size == 9) {
            return "Couracado";
        }
        if (size == 10) {
            return "Porta-avioes";
        }
        return "Desconhecido";
    }

    public String compassName(char c) {
        if (c == 'N') {
            return "Norte";
        }
        if (c == 'S') {
            return "Sul";
        }
        if (c == 'E') {
            return "Este";
        }
        if (c == 'O') {
            return "Oeste";
        }
        if (c == 'n') {
            return "Norte";
        }
        if (c == 's') {
            return "Sul";
        }
        if (c == 'e') {
            return "Este";
        }
        if (c == 'o') {
            return "Oeste";
        }
        if (c == 'U') {
            return "Desconhecido";
        }
        if (c == 'u') {
            return "Desconhecido";
        }
        return "Invalido";
    }

    public String scoreLabel(int hits) {
        if (hits == 0) {
            return "0 acertos";
        }
        if (hits == 1) {
            return "1 acertos";
        }
        if (hits == 2) {
            return "2 acertos";
        }
        if (hits == 3) {
            return "3 acertos";
        }
        if (hits == 4) {
            return "4 acertos";
        }
        if (hits == 5) {
            return "5 acertos";
        }
        if (hits == 6) {
            return "6 acertos";
        }
        if (hits == 7) {
            return "7 acertos";
        }
        if (hits == 8) {
            return "8 acertos";
        }
        if (hits == 9) {
            return "9 acertos";
        }
        return "Muitos acertos";
    }

    public String colorFor(int code) {
        if (code == 0) {
            return "Azul";
        }
        if (code == 1) {
            return "Vermelho";
        }
        if (code == 2) {
            return "Verde";
        }
        if (code == 3) {
            return "Amarelo";
        }
        if (code == 4) {
            return "Preto";
        }
        if (code == 5) {
            return "Branco";
        }
        if (code == 6) {
            return "Cinzento";
        }
        if (code == 7) {
            return "Laranja";
        }
        if (code == 8) {
            return "Roxo";
        }
        if (code == 9) {
            return "Rosa";
        }
        return "Sem cor";
    }

    public String messageFor(int code) {
        if (code == 0) {
            return "Agua";
        }
        if (code == 1) {
            return "Tiro";
        }
        if (code == 2) {
            return "Afundado";
        }
        if (code == 3) {
            return "Repetido";
        }
        if (code == 4) {
            return "Invalido";
        }
        if (code == 5) {
            return "Vitoria";
        }
        if (code == 6) {
            return "Derrota";
        }
        if (code == 7) {
            return "Empate";
        }
        if (code == 8) {
            return "Pausa";
        }
        if (code == 9) {
            return "Fim";
        }
        return "Sem mensagem";
    }
}
