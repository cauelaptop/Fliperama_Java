import java.awt.*;
import java.awt.event.*;
import java.util.Random;
import javax.swing.*;

public class Tetris extends JPanel implements KeyListener, ActionListener {
    private static final int LARGURA = 800;
    private static final int ALTURA = 600;
    
    // Configurações do Tabuleiro do Tetris
    private static final int COLUNAS = 10;
    private static final int LINHAS = 20;
    private static final int TAMANHO_BLOCO = 25;
    
    private final int OFFSET_X = (LARGURA - (COLUNAS * TAMANHO_BLOCO)) / 2;
    private final int OFFSET_Y = (ALTURA - (LINHAS * TAMANHO_BLOCO)) / 2;

    enum Estado { MENU, JOGANDO, FIM_DE_JOGO }
    private Estado estadoAtual = Estado.MENU;

    private int[][] tabuleiro = new int[LINHAS][COLUNAS];
    
    // Cores das peças (Índices 1 a 7)
    private final Color[] CORES = {
        Color.BLACK,
        new Color(0, 255, 255),   // 1: I (Ciano)
        new Color(255, 255, 0),   // 2: O (Amarelo)
        new Color(128, 0, 128),   // 3: T (Roxo)
        new Color(0, 255, 0),     // 4: S (Verde)
        new Color(255, 0, 0),     // 5: Z (Vermelho)
        new Color(0, 0, 255),     // 6: J (Azul)
        new Color(255, 128, 0)    // 7: L (Laranja)
    };

    // Definição das formas
    private final int[][][] FORMAS = {
        {{1, 1, 1, 1}},                               // I
        {{2, 2}, {2, 2}},                             // O
        {{0, 3, 0}, {3, 3, 3}},                       // T
        {{0, 4, 4}, {4, 4, 0}},                       // S
        {{5, 5, 0}, {0, 5, 5}},                       // Z
        {{6, 0, 0}, {6, 6, 6}},                       // J
        {{0, 0, 7}, {7, 7, 7}}                        // L
    };

    private int[][] pecaAtual;
    private int pecaX, pecaY;
    private int[][] proximaPeca;
    
    private int pontos = 0;
    private int linhasFeitas = 0;
    private int nivel = 1;

    private Timer timer;
    private Random random = new Random();
    
    // Controle de queda
    private int frameCount = 0;
    private boolean quedaRapida = false;

    public Tetris() {
        setPreferredSize(new Dimension(LARGURA, ALTURA));
        setBackground(new Color(15, 15, 15));
        setFocusable(true);
        addKeyListener(this);

        timer = new Timer(16, this); // Roda a ~60 FPS
        timer.start();
    }

    private void iniciarJogo() {
        tabuleiro = new int[LINHAS][COLUNAS];
        pontos = 0;
        linhasFeitas = 0;
        nivel = 1;
        proximaPeca = FORMAS[random.nextInt(FORMAS.length)];
        gerarNovaPeca();
        estadoAtual = Estado.JOGANDO;
    }

    private void gerarNovaPeca() {
        pecaAtual = proximaPeca;
        proximaPeca = FORMAS[random.nextInt(FORMAS.length)];
        pecaX = COLUNAS / 2 - pecaAtual[0].length / 2;
        pecaY = 0;

        // Se a nova peça já colidir ao nascer, é Game Over
        if (!posicaoValida(pecaAtual, pecaX, pecaY)) {
            estadoAtual = Estado.FIM_DE_JOGO;
        }
    }

    @Override
    public void actionPerformed(ActionEvent e) {
        if (estadoAtual == Estado.JOGANDO) {
            atualizarJogo();
        }
        repaint();
    }

    private void atualizarJogo() {
        frameCount++;
        int velocidadeGravidade = Math.max(5, 45 - (nivel * 4)); // Fica mais rápido com o nível
        int limiteTique = quedaRapida ? 3 : velocidadeGravidade;

        if (frameCount >= limiteTique) {
            frameCount = 0;
            if (posicaoValida(pecaAtual, pecaX, pecaY + 1)) {
                pecaY++;
            } else {
                fixarPeca();
                verificarLinhas();
                gerarNovaPeca();
            }
        }
    }

    private boolean posicaoValida(int[][] peca, int x, int y) {
        for (int r = 0; r < peca.length; r++) {
            for (int c = 0; c < peca[r].length; c++) {
                if (peca[r][c] != 0) {
                    int nx = x + c;
                    int ny = y + r;
                    // Fora dos limites
                    if (nx < 0 || nx >= COLUNAS || ny >= LINHAS) return false;
                    // Colisão com peças antigas
                    if (ny >= 0 && tabuleiro[ny][nx] != 0) return false;
                }
            }
        }
        return true;
    }

    private void rotacionarPeca() {
        int linhas = pecaAtual.length;
        int colunas = pecaAtual[0].length;
        int[][] novaPeca = new int[colunas][linhas];

        // Gira a matriz em 90 graus
        for (int r = 0; r < linhas; r++) {
            for (int c = 0; c < colunas; c++) {
                novaPeca[c][linhas - 1 - r] = pecaAtual[r][c];
            }
        }

        if (posicaoValida(novaPeca, pecaX, pecaY)) {
            pecaAtual = novaPeca;
        } else if (posicaoValida(novaPeca, pecaX - 1, pecaY)) { // Wall kick simples esq
            pecaAtual = novaPeca;
            pecaX--;
        } else if (posicaoValida(novaPeca, pecaX + 1, pecaY)) { // Wall kick simples dir
            pecaAtual = novaPeca;
            pecaX++;
        }
    }

    private void fixarPeca() {
        for (int r = 0; r < pecaAtual.length; r++) {
            for (int c = 0; c < pecaAtual[r].length; c++) {
                if (pecaAtual[r][c] != 0) {
                    tabuleiro[pecaY + r][pecaX + c] = pecaAtual[r][c];
                }
            }
        }
    }

    private void verificarLinhas() {
        int linhasLimpasAgora = 0;

        for (int r = LINHAS - 1; r >= 0; r--) {
            boolean linhaCheia = true;
            for (int c = 0; c < COLUNAS; c++) {
                if (tabuleiro[r][c] == 0) {
                    linhaCheia = false;
                    break;
                }
            }

            if (linhaCheia) {
                linhasLimpasAgora++;
                // Desce as linhas de cima
                for (int moveY = r; moveY > 0; moveY--) {
                    System.arraycopy(tabuleiro[moveY - 1], 0, tabuleiro[moveY], 0, COLUNAS);
                }
                // Limpa o topo
                tabuleiro[0] = new int[COLUNAS];
                r++; // Verifica a mesma linha novamente
            }
        }

        if (linhasLimpasAgora > 0) {
            linhasFeitas += linhasLimpasAgora;
            // Sistema clássico de pontuação
            if (linhasLimpasAgora == 1) pontos += 100 * nivel;
            else if (linhasLimpasAgora == 2) pontos += 300 * nivel;
            else if (linhasLimpasAgora == 3) pontos += 500 * nivel;
            else if (linhasLimpasAgora == 4) pontos += 800 * nivel; // Tetris!

            nivel = (linhasFeitas / 10) + 1;
        }
    }

    private void quedaInstantanea() {
        while (posicaoValida(pecaAtual, pecaX, pecaY + 1)) {
            pecaY++;
        }
        fixarPeca();
        verificarLinhas();
        gerarNovaPeca();
    }

    @Override
    protected void paintComponent(Graphics g) {
        super.paintComponent(g);
        Graphics2D g2d = (Graphics2D) g;
        g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

        if (estadoAtual == Estado.MENU) {
            desenharMenu(g2d);
        } else {
            desenharJogo(g2d);
            if (estadoAtual == Estado.FIM_DE_JOGO) {
                desenharTelaFinal(g2d);
            }
        }
    }

    private void desenharMenu(Graphics2D g) {
        g.setColor(Color.WHITE);
        g.setFont(new Font("Monospaced", Font.BOLD, 40));
        String titulo = "TETRIS ARCADE";
        g.drawString(titulo, (LARGURA - g.getFontMetrics().stringWidth(titulo)) / 2, 200);

        g.setColor(new Color(0, 255, 255));
        g.setFont(new Font("Monospaced", Font.PLAIN, 20));
        String msg = "Pressione ESPAÇO para iniciar";
        g.drawString(msg, (LARGURA - g.getFontMetrics().stringWidth(msg)) / 2, 300);

        g.setColor(Color.GRAY);
        g.setFont(new Font("Monospaced", Font.PLAIN, 16));
        g.drawString("Setas: Mover/Rotacionar | Espaço: Queda Rápida", 160, 400);
        g.drawString("[ ESC ] VOLTAR A CENTRAL", 280, 480);
    }

    private void desenharJogo(Graphics2D g) {
        // Fundo do Tabuleiro
        g.setColor(new Color(10, 10, 10));
        g.fillRect(OFFSET_X, OFFSET_Y, COLUNAS * TAMANHO_BLOCO, LINHAS * TAMANHO_BLOCO);
        
        // Borda do Tabuleiro
        g.setColor(new Color(80, 80, 80));
        g.drawRect(OFFSET_X - 2, OFFSET_Y - 2, COLUNAS * TAMANHO_BLOCO + 4, LINHAS * TAMANHO_BLOCO + 4);

        // Grade de Fundo
        g.setColor(new Color(30, 30, 30));
        for (int r = 0; r <= LINHAS; r++) g.drawLine(OFFSET_X, OFFSET_Y + r * TAMANHO_BLOCO, OFFSET_X + COLUNAS * TAMANHO_BLOCO, OFFSET_Y + r * TAMANHO_BLOCO);
        for (int c = 0; c <= COLUNAS; c++) g.drawLine(OFFSET_X + c * TAMANHO_BLOCO, OFFSET_Y, OFFSET_X + c * TAMANHO_BLOCO, OFFSET_Y + LINHAS * TAMANHO_BLOCO);

        // Blocos Fixados
        for (int r = 0; r < LINHAS; r++) {
            for (int c = 0; c < COLUNAS; c++) {
                if (tabuleiro[r][c] != 0) {
                    desenharBloco(g, OFFSET_X + c * TAMANHO_BLOCO, OFFSET_Y + r * TAMANHO_BLOCO, CORES[tabuleiro[r][c]]);
                }
            }
        }

        // Peça Atual
        if (pecaAtual != null) {
            // Sombra da Peça (Ghost piece)
            int shadowY = pecaY;
            while (posicaoValida(pecaAtual, pecaX, shadowY + 1)) { shadowY++; }
            for (int r = 0; r < pecaAtual.length; r++) {
                for (int c = 0; c < pecaAtual[r].length; c++) {
                    if (pecaAtual[r][c] != 0) {
                        g.setColor(new Color(255, 255, 255, 40));
                        g.fillRect(OFFSET_X + (pecaX + c) * TAMANHO_BLOCO, OFFSET_Y + (shadowY + r) * TAMANHO_BLOCO, TAMANHO_BLOCO, TAMANHO_BLOCO);
                        g.setColor(new Color(255, 255, 255, 80));
                        g.drawRect(OFFSET_X + (pecaX + c) * TAMANHO_BLOCO, OFFSET_Y + (shadowY + r) * TAMANHO_BLOCO, TAMANHO_BLOCO, TAMANHO_BLOCO);
                    }
                }
            }

            // Peça Real
            for (int r = 0; r < pecaAtual.length; r++) {
                for (int c = 0; c < pecaAtual[r].length; c++) {
                    if (pecaAtual[r][c] != 0) {
                        desenharBloco(g, OFFSET_X + (pecaX + c) * TAMANHO_BLOCO, OFFSET_Y + (pecaY + r) * TAMANHO_BLOCO, CORES[pecaAtual[r][c]]);
                    }
                }
            }
        }

        // HUD - Painel Lateral
        g.setColor(Color.WHITE);
        g.setFont(new Font("Monospaced", Font.BOLD, 22));
        g.drawString("TETRIS", OFFSET_X + COLUNAS * TAMANHO_BLOCO + 50, OFFSET_Y + 30);
        
        g.setFont(new Font("Monospaced", Font.PLAIN, 16));
        g.drawString("NÍVEL: " + nivel, OFFSET_X + COLUNAS * TAMANHO_BLOCO + 50, OFFSET_Y + 80);
        g.drawString("LINHAS: " + linhasFeitas, OFFSET_X + COLUNAS * TAMANHO_BLOCO + 50, OFFSET_Y + 110);
        g.drawString("SCORE: " + pontos, OFFSET_X + COLUNAS * TAMANHO_BLOCO + 50, OFFSET_Y + 140);
        
        g.drawString("PRÓXIMA:", OFFSET_X + COLUNAS * TAMANHO_BLOCO + 50, OFFSET_Y + 200);

        // Desenhar Próxima Peça
        if (proximaPeca != null) {
            for (int r = 0; r < proximaPeca.length; r++) {
                for (int c = 0; c < proximaPeca[r].length; c++) {
                    if (proximaPeca[r][c] != 0) {
                        desenharBloco(g, OFFSET_X + COLUNAS * TAMANHO_BLOCO + 50 + c * TAMANHO_BLOCO, OFFSET_Y + 230 + r * TAMANHO_BLOCO, CORES[proximaPeca[r][c]]);
                    }
                }
            }
        }
    }

    private void desenharBloco(Graphics2D g, int x, int y, Color cor) {
        g.setColor(cor);
        g.fillRect(x, y, TAMANHO_BLOCO, TAMANHO_BLOCO);
        // Efeito 3D nas bordas
        g.setColor(cor.brighter());
        g.drawLine(x, y, x + TAMANHO_BLOCO, y);
        g.drawLine(x, y, x, y + TAMANHO_BLOCO);
        g.setColor(cor.darker().darker());
        g.drawLine(x, y + TAMANHO_BLOCO, x + TAMANHO_BLOCO, y + TAMANHO_BLOCO);
        g.drawLine(x + TAMANHO_BLOCO, y, x + TAMANHO_BLOCO, y + TAMANHO_BLOCO);
    }

    private void desenharTelaFinal(Graphics2D g) {
        g.setColor(new Color(0, 0, 0, 190));
        g.fillRect(0, 0, LARGURA, ALTURA);

        g.setColor(Color.RED);
        g.setFont(new Font("Monospaced", Font.BOLD, 50));
        String txt = "GAME OVER";
        g.drawString(txt, (LARGURA - g.getFontMetrics().stringWidth(txt)) / 2, ALTURA / 2 - 40);

        g.setColor(Color.WHITE);
        g.setFont(new Font("Monospaced", Font.PLAIN, 20));
        String pts = "Pontuação: " + pontos;
        g.drawString(pts, (LARGURA - g.getFontMetrics().stringWidth(pts)) / 2, ALTURA / 2 + 10);

        g.setColor(Color.LIGHT_GRAY);
        g.setFont(new Font("Monospaced", Font.PLAIN, 16));
        String recomecar = "Pressione ESPAÇO para jogar novamente";
        g.drawString(recomecar, (LARGURA - g.getFontMetrics().stringWidth(recomecar)) / 2, ALTURA / 2 + 60);
    }

    @Override
    public void keyPressed(KeyEvent e) {
        int key = e.getKeyCode();

        if (estadoAtual == Estado.MENU) {
            if (key == KeyEvent.VK_SPACE) iniciarJogo();
            if (key == KeyEvent.VK_ESCAPE) {
                JFrame frame = (JFrame) SwingUtilities.getWindowAncestor(this);
                if (frame != null) frame.dispose();
            }
        } else if (estadoAtual == Estado.JOGANDO) {
            if (key == KeyEvent.VK_LEFT) {
                if (posicaoValida(pecaAtual, pecaX - 1, pecaY)) pecaX--;
            }
            if (key == KeyEvent.VK_RIGHT) {
                if (posicaoValida(pecaAtual, pecaX + 1, pecaY)) pecaX++;
            }
            if (key == KeyEvent.VK_UP) rotacionarPeca();
            if (key == KeyEvent.VK_DOWN) quedaRapida = true;
            if (key == KeyEvent.VK_SPACE) quedaInstantanea();
            if (key == KeyEvent.VK_ESCAPE) estadoAtual = Estado.MENU;
        } else if (estadoAtual == Estado.FIM_DE_JOGO) {
            if (key == KeyEvent.VK_SPACE) iniciarJogo();
            if (key == KeyEvent.VK_ESCAPE) estadoAtual = Estado.MENU;
        }
    }

    @Override
    public void keyReleased(KeyEvent e) {
        if (e.getKeyCode() == KeyEvent.VK_DOWN) quedaRapida = false;
    }

    @Override public void keyTyped(KeyEvent e) {}
}