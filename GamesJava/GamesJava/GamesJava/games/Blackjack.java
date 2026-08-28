import java.awt.*;
import java.awt.event.*;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import javax.swing.*;

public class Blackjack extends JPanel implements KeyListener {
    private static final int LARGURA = 800;
    private static final int ALTURA = 600;
    private static final int C_LARG = 80;
    private static final int C_ALT = 120;

    enum Estado { MENU, JOGANDO, FIM }
    private Estado estadoAtual = Estado.MENU;

    class Carta {
        int naipe; // 0=♥, 1=♦, 2=♣, 3=♠
        int valor; // 1 a 13
        boolean virada;

        Carta(int naipe, int valor) {
            this.naipe = naipe;
            this.valor = valor;
            this.virada = false;
        }
        boolean isVermelha() { return naipe < 2; }
    }

    private List<Carta> baralho;
    private List<Carta> maoJogador;
    private List<Carta> maoDealer;
    private String mensagemFim = "";
    private int vitorias = 0, derrotas = 0, empates = 0;

    public Blackjack() {
        setPreferredSize(new Dimension(LARGURA, ALTURA));
        setBackground(new Color(20, 90, 40)); // Verde mesa de cassino
        setFocusable(true);
        addKeyListener(this);
    }

    private void iniciarJogo() {
        baralho = new ArrayList<>();
        maoJogador = new ArrayList<>();
        maoDealer = new ArrayList<>();
        mensagemFim = "";

        // Criar baralho de 52 cartas
        for (int n = 0; n < 4; n++) {
            for (int v = 1; v <= 13; v++) baralho.add(new Carta(n, v));
        }
        Collections.shuffle(baralho);

        // Dar 2 cartas para cada
        maoJogador.add(comprarCarta(true));
        maoDealer.add(comprarCarta(false)); // Carta do dealer escondida
        maoJogador.add(comprarCarta(true));
        maoDealer.add(comprarCarta(true));

        estadoAtual = Estado.JOGANDO;
        verificarBlackjack();
        repaint();
    }

    private Carta comprarCarta(boolean viradaCima) {
        Carta c = baralho.remove(baralho.size() - 1);
        c.virada = viradaCima;
        return c;
    }

    private int calcularPontos(List<Carta> mao) {
        int total = 0;
        int ases = 0;
        for (Carta c : mao) {
            if (!c.virada) continue;
            if (c.valor == 1) {
                ases++;
                total += 11;
            } else if (c.valor > 10) {
                total += 10; // J, Q, K
            } else {
                total += c.valor;
            }
        }
        // Se estourou 21 e tem Ás, o Ás passa a valer 1 (subtrai 10)
        while (total > 21 && ases > 0) {
            total -= 10;
            ases--;
        }
        return total;
    }

    private void verificarBlackjack() {
        int ptsJogador = calcularPontos(maoJogador);
        int ptsDealer = calcularPontos(maoDealer); // Sem a carta virada

        if (ptsJogador == 21) {
            maoDealer.get(0).virada = true; // Revela carta
            if (calcularPontos(maoDealer) == 21) finalizarJogo("Empate! Ambos têm Blackjack.");
            else finalizarJogo("BLACKJACK! Você venceu!");
        }
    }

    private void pedirCarta() {
        maoJogador.add(comprarCarta(true));
        if (calcularPontos(maoJogador) > 21) {
            maoDealer.get(0).virada = true;
            finalizarJogo("ESTOUROU! Você passou de 21.");
        }
        repaint();
    }

    private void manter() {
        // Turno do Dealer
        maoDealer.get(0).virada = true; // Revela a carta
        while (calcularPontos(maoDealer) < 17) {
            maoDealer.add(comprarCarta(true));
        }

        int ptsJogador = calcularPontos(maoJogador);
        int ptsDealer = calcularPontos(maoDealer);

        if (ptsDealer > 21) finalizarJogo("O Dealer estourou! VOCÊ VENCEU!");
        else if (ptsJogador > ptsDealer) finalizarJogo("Você fez mais pontos! VOCÊ VENCEU!");
        else if (ptsDealer > ptsJogador) finalizarJogo("O Dealer fez mais pontos! VOCÊ PERDEU!");
        else finalizarJogo("Mesma pontuação. EMPATE!");
        
        repaint();
    }

    private void finalizarJogo(String msg) {
        mensagemFim = msg;
        estadoAtual = Estado.FIM;
        if (msg.contains("VENCEU")) vitorias++;
        else if (msg.contains("PERDEU") || msg.contains("ESTOUROU")) derrotas++;
        else empates++;
    }

    // --- RENDERIZAÇÃO ---
    @Override
    protected void paintComponent(Graphics g) {
        super.paintComponent(g);
        Graphics2D g2d = (Graphics2D) g;
        g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

        if (estadoAtual == Estado.MENU) {
            desenharMenu(g2d);
        } else {
            desenharJogo(g2d);
        }
    }

    private void desenharMenu(Graphics2D g) {
        g.setColor(Color.WHITE);
        g.setFont(new Font("Monospaced", Font.BOLD, 50));
        g.drawString("BLACKJACK 21", LARGURA / 2 - 170, 200);

        g.setColor(Color.LIGHT_GRAY);
        g.setFont(new Font("Monospaced", Font.PLAIN, 20));
        g.drawString("Como jogar:", LARGURA / 2 - 60, 280);
        g.drawString("[ P ] Pedir Carta (Hit)", LARGURA / 2 - 130, 320);
        g.drawString("[ M ] Manter (Stand)", LARGURA / 2 - 130, 350);

        g.setColor(Color.GREEN);
        g.setFont(new Font("Monospaced", Font.BOLD, 22));
        g.drawString("Pressione ESPAÇO para Iniciar", LARGURA / 2 - 180, 450);
        
        g.setColor(Color.GRAY);
        g.setFont(new Font("Monospaced", Font.PLAIN, 16));
        g.drawString("[ ESC ] VOLTAR A CENTRAL", LARGURA / 2 - 115, 520);
    }

    private void desenharJogo(Graphics2D g) {
        // Placar
        g.setColor(Color.WHITE);
        g.setFont(new Font("Arial", Font.BOLD, 16));
        g.drawString("Vitórias: " + vitorias + " | Derrotas: " + derrotas + " | Empates: " + empates, 20, 30);

        // Dealer
        g.setFont(new Font("Arial", Font.BOLD, 20));
        g.setColor(Color.ORANGE);
        g.drawString("DEALER: " + (estadoAtual == Estado.FIM ? calcularPontos(maoDealer) : "?"), LARGURA / 2 - 50, 80);
        desenharMao(g, maoDealer, 100);

        // Jogador
        g.setColor(Color.CYAN);
        g.drawString("VOCÊ: " + calcularPontos(maoJogador), LARGURA / 2 - 40, 360);
        desenharMao(g, maoJogador, 380);

        // Controles ou Fim
        if (estadoAtual == Estado.JOGANDO) {
            g.setColor(Color.WHITE);
            g.setFont(new Font("Monospaced", Font.BOLD, 22));
            g.drawString("Pressione: [ P ] Pedir  |  [ M ] Manter", LARGURA / 2 - 240, 550);
        } else if (estadoAtual == Estado.FIM) {
            g.setColor(new Color(0, 0, 0, 150));
            g.fillRect(0, ALTURA/2 - 50, LARGURA, 100);
            
            g.setColor(mensagemFim.contains("VENCEU") ? Color.GREEN : mensagemFim.contains("EMPATE") ? Color.YELLOW : Color.RED);
            g.setFont(new Font("Arial", Font.BOLD, 30));
            g.drawString(mensagemFim, LARGURA / 2 - g.getFontMetrics().stringWidth(mensagemFim) / 2, ALTURA / 2 - 10);
            
            g.setColor(Color.WHITE);
            g.setFont(new Font("Monospaced", Font.PLAIN, 18));
            g.drawString("Pressione [ ESPAÇO ] para jogar novamente", LARGURA / 2 - 220, ALTURA / 2 + 25);
        }
    }

    private void desenharMao(Graphics2D g, List<Carta> mao, int y) {
        int wTotal = mao.size() * 40 + C_LARG;
        int startX = (LARGURA - wTotal) / 2 + 20;

        for (int i = 0; i < mao.size(); i++) {
            Carta c = mao.get(i);
            int cx = startX + i * 40;
            if (c.virada) desenharCarta(g, c, cx, y);
            else desenharCostas(g, cx, y);
        }
    }

    private void desenharCarta(Graphics2D g, Carta c, int x, int y) {
        g.setColor(Color.WHITE);
        g.fillRoundRect(x, y, C_LARG, C_ALT, 10, 10);
        g.setColor(Color.BLACK);
        g.drawRoundRect(x, y, C_LARG, C_ALT, 10, 10);

        String[] nStr = {"♥", "♦", "♣", "♠"};
        String[] vStr = {"", "A", "2", "3", "4", "5", "6", "7", "8", "9", "10", "J", "Q", "K"};

        g.setColor(c.isVermelha() ? Color.RED : Color.BLACK);
        g.setFont(new Font("Arial", Font.BOLD, 18));
        g.drawString(vStr[c.valor], x + 5, y + 20);
        g.drawString(nStr[c.naipe], x + 5, y + 40);
        g.setFont(new Font("Arial", Font.BOLD, 35));
        g.drawString(nStr[c.naipe], x + 25, y + 80);
    }

    private void desenharCostas(Graphics2D g, int x, int y) {
        g.setColor(Color.BLUE.darker());
        g.fillRoundRect(x, y, C_LARG, C_ALT, 10, 10);
        g.setColor(Color.WHITE);
        g.drawRoundRect(x, y, C_LARG, C_ALT, 10, 10);
        g.drawRoundRect(x + 5, y + 5, C_LARG - 10, C_ALT - 10, 5, 5);
    }

    // --- CONTROLES ---
    @Override
    public void keyPressed(KeyEvent e) {
        int key = e.getKeyCode();
        if (key == KeyEvent.VK_ESCAPE) {
            if (estadoAtual == Estado.MENU) {
                JFrame frame = (JFrame) SwingUtilities.getWindowAncestor(this);
                if (frame != null) frame.dispose();
            } else {
                estadoAtual = Estado.MENU; repaint();
            }
        }
        else if (key == KeyEvent.VK_SPACE) {
            if (estadoAtual != Estado.JOGANDO) iniciarJogo();
        }
        else if (estadoAtual == Estado.JOGANDO) {
            if (key == KeyEvent.VK_P) pedirCarta();
            if (key == KeyEvent.VK_M) manter();
        }
    }
    @Override public void keyReleased(KeyEvent e) {}
    @Override public void keyTyped(KeyEvent e) {}
}