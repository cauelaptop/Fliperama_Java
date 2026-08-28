import java.awt.*;
import java.awt.event.*;
import java.util.ArrayList;
import java.util.Random;
import javax.swing.*;

public class JogodaVelha extends JPanel implements MouseListener, KeyListener {
    private static final int LARGURA = 800;
    private static final int ALTURA = 600;

    enum Estado { MENU, JOGANDO, FIM }
    private Estado estadoAtual = Estado.MENU;

    private int dificuldade = 1; // 1 = Fácil, 2 = Médio, 3 = Difícil

    private char[][] tabuleiro = new char[3][3];
    private boolean turnoJogador = true; // true = X (Jogador), false = O (CPU)
    private String mensagemFim = "";

    private final int TAM_BLOCO = 150;
    private final int OFFSET_X = (LARGURA - (3 * TAM_BLOCO)) / 2;
    private final int OFFSET_Y = (ALTURA - (3 * TAM_BLOCO)) / 2 + 20;

    public JogodaVelha() {
        setPreferredSize(new Dimension(LARGURA, ALTURA));
        setBackground(new Color(20, 20, 30));
        setFocusable(true);
        addMouseListener(this);
        addKeyListener(this);
    }

    private void iniciarJogo() {
        for (int r = 0; r < 3; r++) {
            for (int c = 0; c < 3; c++) {
                tabuleiro[r][c] = '-';
            }
        }
        turnoJogador = true;
        estadoAtual = Estado.JOGANDO;
        repaint();
    }

    private void jogarCPU() {
        if (estadoAtual != Estado.JOGANDO) return;

        int[] jogada = new int[]{-1, -1};
        
        if (dificuldade == 1) {
            jogada = jogadaAleatoria();
        } else if (dificuldade == 2) {
            jogada = jogadaMedia();
        } else {
            jogada = melhorJogadaMinimax();
        }

        if (jogada[0] != -1) {
            tabuleiro[jogada[0]][jogada[1]] = 'O';
            checarEstadoJogo();
            turnoJogador = true;
        }
    }

    // --- IAs DA CPU ---
    private int[] jogadaAleatoria() {
        ArrayList<int[]> vazios = new ArrayList<>();
        for (int r = 0; r < 3; r++) {
            for (int c = 0; c < 3; c++) {
                if (tabuleiro[r][c] == '-') vazios.add(new int[]{r, c});
            }
        }
        if (vazios.isEmpty()) return new int[]{-1, -1};
        return vazios.get(new Random().nextInt(vazios.size()));
    }

    private int[] jogadaMedia() {
        // Tenta vencer
        int[] ataque = checarQuaseVitoria('O');
        if (ataque != null) return ataque;
        // Tenta bloquear jogador
        int[] defesa = checarQuaseVitoria('X');
        if (defesa != null) return defesa;
        // Joga aleatório
        return jogadaAleatoria();
    }

    private int[] checarQuaseVitoria(char jogador) {
        // Simula jogada
        for (int r = 0; r < 3; r++) {
            for (int c = 0; c < 3; c++) {
                if (tabuleiro[r][c] == '-') {
                    tabuleiro[r][c] = jogador;
                    if (verificarVitoria(jogador)) {
                        tabuleiro[r][c] = '-';
                        return new int[]{r, c};
                    }
                    tabuleiro[r][c] = '-';
                }
            }
        }
        return null;
    }

    private int[] melhorJogadaMinimax() {
        int melhorPlacar = Integer.MIN_VALUE;
        int[] jogada = new int[]{-1, -1};

        for (int r = 0; r < 3; r++) {
            for (int c = 0; c < 3; c++) {
                if (tabuleiro[r][c] == '-') {
                    tabuleiro[r][c] = 'O';
                    int placar = minimax(tabuleiro, 0, false);
                    tabuleiro[r][c] = '-';
                    if (placar > melhorPlacar) {
                        melhorPlacar = placar;
                        jogada = new int[]{r, c};
                    }
                }
            }
        }
        return jogada;
    }

    private int minimax(char[][] board, int depth, boolean isMaximizing) {
        if (verificarVitoria('O')) return 10 - depth;
        if (verificarVitoria('X')) return -10 + depth;
        if (isEmpate()) return 0;

        if (isMaximizing) {
            int maxEval = Integer.MIN_VALUE;
            for (int r = 0; r < 3; r++) {
                for (int c = 0; c < 3; c++) {
                    if (board[r][c] == '-') {
                        board[r][c] = 'O';
                        int eval = minimax(board, depth + 1, false);
                        board[r][c] = '-';
                        maxEval = Math.max(maxEval, eval);
                    }
                }
            }
            return maxEval;
        } else {
            int minEval = Integer.MAX_VALUE;
            for (int r = 0; r < 3; r++) {
                for (int c = 0; c < 3; c++) {
                    if (board[r][c] == '-') {
                        board[r][c] = 'X';
                        int eval = minimax(board, depth + 1, true);
                        board[r][c] = '-';
                        minEval = Math.min(minEval, eval);
                    }
                }
            }
            return minEval;
        }
    }

    // --- REGRAS DO JOGO ---
    private void checarEstadoJogo() {
        if (verificarVitoria('X')) {
            mensagemFim = "VOCÊ VENCEU!";
            estadoAtual = Estado.FIM;
        } else if (verificarVitoria('O')) {
            mensagemFim = "CPU VENCEU!";
            estadoAtual = Estado.FIM;
        } else if (isEmpate()) {
            mensagemFim = "DEU VELHA! EMPATE!";
            estadoAtual = Estado.FIM;
        }
    }

    private boolean verificarVitoria(char p) {
        for (int i = 0; i < 3; i++) {
            if (tabuleiro[i][0] == p && tabuleiro[i][1] == p && tabuleiro[i][2] == p) return true;
            if (tabuleiro[0][i] == p && tabuleiro[1][i] == p && tabuleiro[2][i] == p) return true;
        }
        if (tabuleiro[0][0] == p && tabuleiro[1][1] == p && tabuleiro[2][2] == p) return true;
        if (tabuleiro[0][2] == p && tabuleiro[1][1] == p && tabuleiro[2][0] == p) return true;
        return false;
    }

    private boolean isEmpate() {
        for (int r = 0; r < 3; r++) {
            for (int c = 0; c < 3; c++) {
                if (tabuleiro[r][c] == '-') return false;
            }
        }
        return true;
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
            if (estadoAtual == Estado.FIM) {
                desenharFim(g2d);
            }
        }
    }

    private void desenharMenu(Graphics2D g) {
        g.setColor(Color.WHITE);
        g.setFont(new Font("Monospaced", Font.BOLD, 55));
        g.drawString("JOGO DA VELHA", LARGURA / 2 - 220, 180);

        g.setColor(Color.YELLOW);
        g.setFont(new Font("Monospaced", Font.BOLD, 22));
        String dif = "< DIFICULDADE: " + (dificuldade == 1 ? "FÁCIL" : dificuldade == 2 ? "MÉDIO" : "IMPOSSÍVEL") + " >";
        g.drawString(dif, (LARGURA - g.getFontMetrics().stringWidth(dif)) / 2, 300);

        g.setFont(new Font("Monospaced", Font.PLAIN, 14));
        g.drawString("(Setas Esq/Dir para mudar)", LARGURA / 2 - 110, 330);

        g.setColor(Color.GREEN);
        g.setFont(new Font("Monospaced", Font.BOLD, 20));
        g.drawString("Pressione ESPAÇO para Iniciar", LARGURA / 2 - 170, 420);
        
        g.setColor(Color.GRAY);
        g.setFont(new Font("Monospaced", Font.PLAIN, 16));
        g.drawString("[ ESC ] VOLTAR A CENTRAL", LARGURA / 2 - 115, 480);
    }

    private void desenharJogo(Graphics2D g) {
        g.setColor(Color.WHITE);
        g.setStroke(new BasicStroke(8, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND));

        // Grade
        g.drawLine(OFFSET_X + TAM_BLOCO, OFFSET_Y, OFFSET_X + TAM_BLOCO, OFFSET_Y + 3 * TAM_BLOCO);
        g.drawLine(OFFSET_X + 2 * TAM_BLOCO, OFFSET_Y, OFFSET_X + 2 * TAM_BLOCO, OFFSET_Y + 3 * TAM_BLOCO);
        g.drawLine(OFFSET_X, OFFSET_Y + TAM_BLOCO, OFFSET_X + 3 * TAM_BLOCO, OFFSET_Y + TAM_BLOCO);
        g.drawLine(OFFSET_X, OFFSET_Y + 2 * TAM_BLOCO, OFFSET_X + 3 * TAM_BLOCO, OFFSET_Y + 2 * TAM_BLOCO);

        // Peças
        for (int r = 0; r < 3; r++) {
            for (int c = 0; c < 3; c++) {
                int px = OFFSET_X + c * TAM_BLOCO + 30;
                int py = OFFSET_Y + r * TAM_BLOCO + 30;
                int tam = TAM_BLOCO - 60;

                if (tabuleiro[r][c] == 'X') {
                    g.setColor(new Color(255, 80, 80));
                    g.drawLine(px, py, px + tam, py + tam);
                    g.drawLine(px + tam, py, px, py + tam);
                } else if (tabuleiro[r][c] == 'O') {
                    g.setColor(new Color(80, 150, 255));
                    g.drawOval(px, py, tam, tam);
                }
            }
        }
    }

    private void desenharFim(Graphics2D g) {
        g.setColor(new Color(0, 0, 0, 180));
        g.fillRect(0, 0, LARGURA, ALTURA);

        g.setColor(mensagemFim.contains("VOCÊ") ? Color.GREEN : (mensagemFim.contains("CPU") ? Color.RED : Color.YELLOW));
        g.setFont(new Font("Monospaced", Font.BOLD, 45));
        g.drawString(mensagemFim, (LARGURA - g.getFontMetrics().stringWidth(mensagemFim)) / 2, ALTURA / 2 - 20);

        g.setColor(Color.WHITE);
        g.setFont(new Font("Monospaced", Font.PLAIN, 18));
        String recomecar = "Pressione ESPAÇO para voltar ao Menu";
        g.drawString(recomecar, (LARGURA - g.getFontMetrics().stringWidth(recomecar)) / 2, ALTURA / 2 + 50);
    }

    // --- INPUTS ---
    @Override
    public void mouseReleased(MouseEvent e) {
        if (estadoAtual != Estado.JOGANDO || !turnoJogador) return;

        int mx = e.getX();
        int my = e.getY();

        if (mx > OFFSET_X && mx < OFFSET_X + 3 * TAM_BLOCO &&
            my > OFFSET_Y && my < OFFSET_Y + 3 * TAM_BLOCO) {
            
            int c = (mx - OFFSET_X) / TAM_BLOCO;
            int r = (my - OFFSET_Y) / TAM_BLOCO;

            if (tabuleiro[r][c] == '-') {
                tabuleiro[r][c] = 'X';
                checarEstadoJogo();
                repaint();
                
                if (estadoAtual == Estado.JOGANDO) {
                    turnoJogador = false;
                    // Timer para dar a sensação de que a CPU está "pensando"
                    Timer t = new Timer(500, evt -> {
                        jogarCPU();
                        repaint();
                    });
                    t.setRepeats(false);
                    t.start();
                }
            }
        }
    }
    @Override public void mouseClicked(MouseEvent e) {}
    @Override public void mousePressed(MouseEvent e) {}
    @Override public void mouseEntered(MouseEvent e) {}
    @Override public void mouseExited(MouseEvent e) {}

    @Override
    public void keyPressed(KeyEvent e) {
        int key = e.getKeyCode();
        if (estadoAtual == Estado.MENU) {
            if (key == KeyEvent.VK_LEFT) dificuldade = Math.max(1, dificuldade - 1);
            if (key == KeyEvent.VK_RIGHT) dificuldade = Math.min(3, dificuldade + 1);
            if (key == KeyEvent.VK_SPACE) iniciarJogo();
            if (key == KeyEvent.VK_ESCAPE) {
                JFrame frame = (JFrame) SwingUtilities.getWindowAncestor(this);
                if (frame != null) frame.dispose();
            }
        } else if (estadoAtual == Estado.JOGANDO) {
            if (key == KeyEvent.VK_ESCAPE) estadoAtual = Estado.MENU;
        } else {
            if (key == KeyEvent.VK_SPACE || key == KeyEvent.VK_ESCAPE) estadoAtual = Estado.MENU;
        }
        repaint();
    }
    @Override public void keyReleased(KeyEvent e) {}
    @Override public void keyTyped(KeyEvent e) {}
}