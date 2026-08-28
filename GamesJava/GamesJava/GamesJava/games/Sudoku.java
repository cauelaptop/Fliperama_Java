import java.awt.*;
import java.awt.event.*;
import java.util.ArrayList;
import java.util.Collections;
import javax.swing.*;

public class Sudoku extends JPanel implements MouseListener, KeyListener {
    private static final int LARGURA = 800;
    private static final int ALTURA = 600;

    enum Estado { MENU, JOGANDO, VITORIA }
    private Estado estadoAtual = Estado.MENU;

    private int dificuldade = 1; // 1 = Fácil, 2 = Médio, 3 = Difícil

    private int[][] solucao = new int[9][9];
    private int[][] tabuleiro = new int[9][9];
    private boolean[][] fixo = new boolean[9][9];

    private final int TAM_BLOCO = 50;
    private final int OFFSET_X = (LARGURA - (9 * TAM_BLOCO)) / 2;
    private final int OFFSET_Y = (ALTURA - (9 * TAM_BLOCO)) / 2 + 20;

    private int linSelecionada = -1;
    private int colSelecionada = -1;

    public Sudoku() {
        setPreferredSize(new Dimension(LARGURA, ALTURA));
        setBackground(new Color(20, 20, 25));
        setFocusable(true);
        addMouseListener(this);
        addKeyListener(this);
    }

    private void iniciarJogo() {
        // Zera as matrizes
        solucao = new int[9][9];
        tabuleiro = new int[9][9];
        fixo = new boolean[9][9];
        linSelecionada = -1;
        colSelecionada = -1;

        // 1. Gera um tabuleiro completo válido
        gerarSolucao(0, 0);

        // 2. Copia para o tabuleiro do jogador e define os fixos
        for (int r = 0; r < 9; r++) {
            for (int c = 0; c < 9; c++) {
                tabuleiro[r][c] = solucao[r][c];
                fixo[r][c] = true;
            }
        }

        // 3. Remove números baseado na dificuldade
        int casasParaRemover = dificuldade == 1 ? 35 : (dificuldade == 2 ? 45 : 58);
        removerCasas(casasParaRemover);

        estadoAtual = Estado.JOGANDO;
        repaint();
    }

    // --- ALGORITMO GERADOR DE SUDOKU (BACKTRACKING) ---
    private boolean gerarSolucao(int r, int c) {
        if (r == 9) return true; // Terminou
        
        int proxR = (c == 8) ? r + 1 : r;
        int proxC = (c == 8) ? 0 : c + 1;

        ArrayList<Integer> numeros = new ArrayList<>();
        for (int i = 1; i <= 9; i++) numeros.add(i);
        Collections.shuffle(numeros); // Embaralha para gerar jogos únicos

        for (int num : numeros) {
            if (posicaoValida(solucao, r, c, num)) {
                solucao[r][c] = num;
                if (gerarSolucao(proxR, proxC)) return true;
                solucao[r][c] = 0; // Volta atrás se não der certo
            }
        }
        return false;
    }

    private boolean posicaoValida(int[][] grade, int r, int c, int num) {
        // Checa Linha e Coluna
        for (int i = 0; i < 9; i++) {
            if (grade[r][i] == num) return false;
            if (grade[i][c] == num) return false;
        }
        // Checa Quadrante 3x3
        int inicioR = (r / 3) * 3;
        int inicioC = (c / 3) * 3;
        for (int i = 0; i < 3; i++) {
            for (int j = 0; j < 3; j++) {
                if (grade[inicioR + i][inicioC + j] == num) return false;
            }
        }
        return true;
    }

    private void removerCasas(int qtd) {
        int removidas = 0;
        while (removidas < qtd) {
            int r = (int) (Math.random() * 9);
            int c = (int) (Math.random() * 9);
            if (fixo[r][c]) {
                fixo[r][c] = false;
                tabuleiro[r][c] = 0;
                removidas++;
            }
        }
    }

    private void verificarVitoria() {
        for (int r = 0; r < 9; r++) {
            for (int c = 0; c < 9; c++) {
                if (tabuleiro[r][c] != solucao[r][c]) return; // Ainda tem erros ou vazios
            }
        }
        estadoAtual = Estado.VITORIA;
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
            if (estadoAtual == Estado.VITORIA) {
                desenharFim(g2d, "SUDOKU RESOLVIDO!", Color.GREEN);
            }
        }
    }

    private void desenharMenu(Graphics2D g) {
        g.setColor(Color.WHITE);
        g.setFont(new Font("Monospaced", Font.BOLD, 60));
        g.drawString("SUDOKU", LARGURA / 2 - 105, 180);

        g.setColor(Color.YELLOW);
        g.setFont(new Font("Monospaced", Font.BOLD, 22));
        String dif = "< DIFICULDADE: " + (dificuldade == 1 ? "FÁCIL" : dificuldade == 2 ? "MÉDIO" : "DIFÍCIL") + " >";
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
        // Título ingame
        g.setColor(Color.WHITE);
        g.setFont(new Font("Monospaced", Font.BOLD, 24));
        g.drawString("SUDOKU - " + (dificuldade == 1 ? "FÁCIL" : dificuldade == 2 ? "MÉDIO" : "DIFÍCIL"), OFFSET_X, OFFSET_Y - 20);

        // Fundo e Seleção
        for (int r = 0; r < 9; r++) {
            for (int c = 0; c < 9; c++) {
                int px = OFFSET_X + c * TAM_BLOCO;
                int py = OFFSET_Y + r * TAM_BLOCO;

                if (r == linSelecionada && c == colSelecionada) {
                    g.setColor(new Color(60, 60, 80)); // Cor da célula selecionada
                } else {
                    g.setColor(new Color(40, 40, 50)); // Cor padrão
                }
                g.fillRect(px, py, TAM_BLOCO, TAM_BLOCO);

                // Desenha os números e Feedbacks
                if (tabuleiro[r][c] != 0) {
                    if (fixo[r][c]) {
                        g.setColor(Color.WHITE); // Fixo do tabuleiro
                    } else if (tabuleiro[r][c] == solucao[r][c]) {
                        g.setColor(new Color(80, 255, 80)); // Certo (Verde)
                    } else {
                        g.setColor(new Color(255, 80, 80)); // Errado (Vermelho)
                    }

                    g.setFont(new Font("Monospaced", Font.BOLD, 28));
                    String num = String.valueOf(tabuleiro[r][c]);
                    FontMetrics fm = g.getFontMetrics();
                    int nx = px + (TAM_BLOCO - fm.stringWidth(num)) / 2;
                    int ny = py + fm.getAscent() + (TAM_BLOCO - fm.getHeight()) / 2;
                    g.drawString(num, nx, ny);
                }
            }
        }

        // Linhas da Grade
        for (int i = 0; i <= 9; i++) {
            if (i % 3 == 0) {
                g.setStroke(new BasicStroke(3)); // Linhas grossas pros quadrantes
                g.setColor(Color.WHITE);
            } else {
                g.setStroke(new BasicStroke(1)); // Linhas finas
                g.setColor(Color.GRAY);
            }
            // Verticais
            g.drawLine(OFFSET_X + i * TAM_BLOCO, OFFSET_Y, OFFSET_X + i * TAM_BLOCO, OFFSET_Y + 9 * TAM_BLOCO);
            // Horizontais
            g.drawLine(OFFSET_X, OFFSET_Y + i * TAM_BLOCO, OFFSET_X + 9 * TAM_BLOCO, OFFSET_Y + i * TAM_BLOCO);
        }
    }

    private void desenharFim(Graphics2D g, String mensagem, Color cor) {
        g.setColor(new Color(0, 0, 0, 200));
        g.fillRect(0, 0, LARGURA, ALTURA);

        g.setColor(cor);
        g.setFont(new Font("Monospaced", Font.BOLD, 45));
        g.drawString(mensagem, (LARGURA - g.getFontMetrics().stringWidth(mensagem)) / 2, ALTURA / 2 - 20);

        g.setColor(Color.WHITE);
        g.setFont(new Font("Monospaced", Font.PLAIN, 18));
        String recomecar = "Pressione ESPAÇO para voltar ao Menu";
        g.drawString(recomecar, (LARGURA - g.getFontMetrics().stringWidth(recomecar)) / 2, ALTURA / 2 + 50);
    }

    // --- CONTROLES MOUSE (SELECIONAR CASA) ---
    @Override
    public void mouseReleased(MouseEvent e) {
        if (estadoAtual != Estado.JOGANDO) return;

        int mx = e.getX();
        int my = e.getY();

        if (mx > OFFSET_X && mx < OFFSET_X + 9 * TAM_BLOCO &&
            my > OFFSET_Y && my < OFFSET_Y + 9 * TAM_BLOCO) {
            
            int c = (mx - OFFSET_X) / TAM_BLOCO;
            int r = (my - OFFSET_Y) / TAM_BLOCO;

            if (!fixo[r][c]) {
                linSelecionada = r;
                colSelecionada = c;
            } else {
                linSelecionada = -1; // Clicou num fixo, deseleciona
                colSelecionada = -1;
            }
            repaint();
        }
    }
    @Override public void mouseClicked(MouseEvent e) {}
    @Override public void mousePressed(MouseEvent e) {}
    @Override public void mouseEntered(MouseEvent e) {}
    @Override public void mouseExited(MouseEvent e) {}

    // --- CONTROLES TECLADO (DIGITAR NÚMERO) ---
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
            if (key == KeyEvent.VK_ESCAPE) {
                estadoAtual = Estado.MENU;
            } else if (linSelecionada != -1 && colSelecionada != -1) {
                // Se apertar Backspace ou Delete, apaga o número
                if (key == KeyEvent.VK_BACK_SPACE || key == KeyEvent.VK_DELETE) {
                    tabuleiro[linSelecionada][colSelecionada] = 0;
                }
                // Se digitar de 1 a 9
                else if (key >= KeyEvent.VK_1 && key <= KeyEvent.VK_9) {
                    tabuleiro[linSelecionada][colSelecionada] = key - KeyEvent.VK_0;
                    verificarVitoria();
                } 
                // Suporte também para o teclado numérico lateral (Numpad)
                else if (key >= KeyEvent.VK_NUMPAD1 && key <= KeyEvent.VK_NUMPAD9) {
                    tabuleiro[linSelecionada][colSelecionada] = key - KeyEvent.VK_NUMPAD0;
                    verificarVitoria();
                }
            }
        } else if (estadoAtual == Estado.VITORIA) {
            if (key == KeyEvent.VK_SPACE || key == KeyEvent.VK_ESCAPE) estadoAtual = Estado.MENU;
        }
        repaint();
    }
    @Override public void keyReleased(KeyEvent e) {}
    @Override public void keyTyped(KeyEvent e) {}
}