import java.awt.*;
import java.awt.event.*;
import java.util.ArrayList;
import java.util.Collections;
import javax.swing.*;

public class JogoDaMemoria extends JPanel implements MouseListener, KeyListener {
    private static final int LARGURA = 800;
    private static final int ALTURA = 600;

    enum Estado { MENU, JOGANDO, VITORIA }
    private Estado estadoAtual = Estado.MENU;

    private int dificuldade = 1; // 1 = Fácil, 2 = Médio, 3 = Difícil

    private int linhas, colunas;
    private String[][] tabuleiro;
    private boolean[][] revelado;
    private boolean[][] resolvido;

    private int paresEncontrados = 0;
    private int totalPares = 0;
    private int tentativas = 0;

    private int carta1L = -1, carta1C = -1;
    private int carta2L = -1, carta2C = -1;
    private boolean processando = false; // Impede cliques enquanto mostra cartas erradas

    // Cartas (usamos letras maiúsculas para facilitar a renderização universal)
    private final String[] icones = {
        "A", "B", "C", "D", "E", "F", "G", "H", "I", 
        "J", "K", "L", "M", "N", "O", "P", "Q", "R"
    };

    private final int TAM_CARTA = 80;
    private final int ESPACO = 10;
    private int offsetX, offsetY;

    public JogoDaMemoria() {
        setPreferredSize(new Dimension(LARGURA, ALTURA));
        setBackground(new Color(30, 40, 50));
        setFocusable(true);
        addMouseListener(this);
        addKeyListener(this);
    }

    private void iniciarJogo() {
        if (dificuldade == 1) {
            linhas = 4; colunas = 4; // 16 cartas
        } else if (dificuldade == 2) {
            linhas = 4; colunas = 5; // 20 cartas
        } else {
            linhas = 6; colunas = 6; // 36 cartas
        }

        tabuleiro = new String[linhas][colunas];
        revelado = new boolean[linhas][colunas];
        resolvido = new boolean[linhas][colunas];
        
        totalPares = (linhas * colunas) / 2;
        paresEncontrados = 0;
        tentativas = 0;
        carta1L = -1; carta1C = -1;
        processando = false;

        // Centralizar tabuleiro na tela
        offsetX = (LARGURA - (colunas * TAM_CARTA + (colunas - 1) * ESPACO)) / 2;
        offsetY = (ALTURA - (linhas * TAM_CARTA + (linhas - 1) * ESPACO)) / 2 + 20;

        gerarCartas();
        estadoAtual = Estado.JOGANDO;
        repaint();
    }

    private void gerarCartas() {
        ArrayList<String> baralho = new ArrayList<>();
        
        // Pega a quantidade necessária de pares e duplica
        for (int i = 0; i < totalPares; i++) {
            baralho.add(icones[i]);
            baralho.add(icones[i]);
        }
        
        // Embaralha
        Collections.shuffle(baralho);

        // Distribui no tabuleiro
        int index = 0;
        for (int r = 0; r < linhas; r++) {
            for (int c = 0; c < colunas; c++) {
                tabuleiro[r][c] = baralho.get(index++);
            }
        }
    }

    private void processarJogada() {
        tentativas++;
        if (tabuleiro[carta1L][carta1C].equals(tabuleiro[carta2L][carta2C])) {
            // Acertou
            resolvido[carta1L][carta1C] = true;
            resolvido[carta2L][carta2C] = true;
            paresEncontrados++;
            resetarSelecao();
            
            if (paresEncontrados == totalPares) {
                estadoAtual = Estado.VITORIA;
            }
        } else {
            // Errou - Aguarda 1 segundo e esconde
            Timer t = new Timer(1000, e -> {
                revelado[carta1L][carta1C] = false;
                revelado[carta2L][carta2C] = false;
                resetarSelecao();
                repaint();
            });
            t.setRepeats(false);
            t.start();
        }
    }

    private void resetarSelecao() {
        carta1L = -1; carta1C = -1;
        carta2L = -1; carta2C = -1;
        processando = false;
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
                desenharFim(g2d);
            }
        }
    }

    private void desenharMenu(Graphics2D g) {
        g.setColor(Color.WHITE);
        g.setFont(new Font("Monospaced", Font.BOLD, 50));
        g.drawString("JOGO DA MEMÓRIA", LARGURA / 2 - 220, 180);

        g.setColor(Color.YELLOW);
        g.setFont(new Font("Monospaced", Font.BOLD, 22));
        String dif = "< DIFICULDADE: " + (dificuldade == 1 ? "FÁCIL (4x4)" : dificuldade == 2 ? "MÉDIO (4x5)" : "DIFÍCIL (6x6)") + " >";
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
        // Título e Placar
        g.setColor(Color.WHITE);
        g.setFont(new Font("Monospaced", Font.BOLD, 20));
        g.drawString("PARES: " + paresEncontrados + "/" + totalPares, 20, 30);
        g.drawString("TENTATIVAS: " + tentativas, LARGURA - 180, 30);

        // Cartas
        for (int r = 0; r < linhas; r++) {
            for (int c = 0; c < colunas; c++) {
                int px = offsetX + c * (TAM_CARTA + ESPACO);
                int py = offsetY + r * (TAM_CARTA + ESPACO);

                if (revelado[r][c] || resolvido[r][c]) {
                    // Frente da carta
                    g.setColor(resolvido[r][c] ? new Color(100, 255, 100) : Color.WHITE);
                    g.fillRoundRect(px, py, TAM_CARTA, TAM_CARTA, 15, 15);
                    
                    g.setColor(Color.BLACK);
                    g.setStroke(new BasicStroke(2));
                    g.drawRoundRect(px, py, TAM_CARTA, TAM_CARTA, 15, 15);

                    // Desenha a Letra
                    g.setFont(new Font("Arial", Font.BOLD, 40));
                    FontMetrics fm = g.getFontMetrics();
                    String simbolo = tabuleiro[r][c];
                    int tx = px + (TAM_CARTA - fm.stringWidth(simbolo)) / 2;
                    int ty = py + fm.getAscent() + (TAM_CARTA - fm.getHeight()) / 2;
                    g.drawString(simbolo, tx, ty);
                } else {
                    // Verso da carta
                    g.setColor(new Color(60, 120, 200));
                    g.fillRoundRect(px, py, TAM_CARTA, TAM_CARTA, 15, 15);
                    
                    g.setColor(new Color(40, 80, 150));
                    g.setStroke(new BasicStroke(4));
                    g.drawRoundRect(px + 4, py + 4, TAM_CARTA - 8, TAM_CARTA - 8, 10, 10);
                }
            }
        }
    }

    private void desenharFim(Graphics2D g) {
        g.setColor(new Color(0, 0, 0, 200));
        g.fillRect(0, 0, LARGURA, ALTURA);

        g.setColor(Color.GREEN);
        g.setFont(new Font("Monospaced", Font.BOLD, 45));
        String msg = "TODOS OS PARES ENCONTRADOS!";
        g.drawString(msg, (LARGURA - g.getFontMetrics().stringWidth(msg)) / 2, ALTURA / 2 - 20);

        g.setColor(Color.WHITE);
        g.setFont(new Font("Monospaced", Font.PLAIN, 18));
        String recomecar = "Pressione ESPAÇO para voltar ao Menu";
        g.drawString(recomecar, (LARGURA - g.getFontMetrics().stringWidth(recomecar)) / 2, ALTURA / 2 + 40);
    }

    // --- CONTROLES MOUSE ---
    @Override
    public void mouseReleased(MouseEvent e) {
        if (estadoAtual != Estado.JOGANDO || processando) return;

        int mx = e.getX();
        int my = e.getY();

        for (int r = 0; r < linhas; r++) {
            for (int c = 0; c < colunas; c++) {
                int px = offsetX + c * (TAM_CARTA + ESPACO);
                int py = offsetY + r * (TAM_CARTA + ESPACO);

                // Se clicou dentro desta carta e ela não está revelada
                if (mx >= px && mx <= px + TAM_CARTA && my >= py && my <= py + TAM_CARTA) {
                    if (!revelado[r][c] && !resolvido[r][c]) {
                        revelado[r][c] = true;

                        if (carta1L == -1) {
                            carta1L = r; carta1C = c;
                        } else {
                            carta2L = r; carta2C = c;
                            processando = true; // Bloqueia cliques temporariamente
                            processarJogada();
                        }
                        repaint();
                        return; // Sai do loop após achar a carta clicada
                    }
                }
            }
        }
    }
    @Override public void mouseClicked(MouseEvent e) {}
    @Override public void mousePressed(MouseEvent e) {}
    @Override public void mouseEntered(MouseEvent e) {}
    @Override public void mouseExited(MouseEvent e) {}

    // --- CONTROLES TECLADO ---
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
            repaint();
        } else if (estadoAtual == Estado.JOGANDO) {
            if (key == KeyEvent.VK_ESCAPE) {
                estadoAtual = Estado.MENU;
                repaint();
            }
        } else if (estadoAtual == Estado.VITORIA) {
            if (key == KeyEvent.VK_SPACE || key == KeyEvent.VK_ESCAPE) {
                estadoAtual = Estado.MENU;
                repaint();
            }
        }
    }
    @Override public void keyReleased(KeyEvent e) {}
    @Override public void keyTyped(KeyEvent e) {}
}