import java.awt.*;
import java.awt.event.*;
import java.util.ArrayList;
import java.util.List;
import javax.swing.*;

public class Damas extends JPanel implements MouseListener, KeyListener {
    private static final int LARGURA = 800;
    private static final int ALTURA = 600;
    private static final int TAM_CASA = 60;
    private static final int OFFSET_X = (LARGURA - 8 * TAM_CASA) / 2;
    private static final int OFFSET_Y = (ALTURA - 8 * TAM_CASA) / 2 + 20;

    enum Estado { MENU, JOGANDO, GAMEOVER }
    private Estado estadoAtual = Estado.MENU;
    private int dificuldade = 1; // 1 = Fácil, 2 = Médio, 3 = Difícil

    // 0 = Vazio, 1 = Jogador (Vermelho), 2 = Bot (Preto), 3 = Dama Jogador, 4 = Dama Bot
    private int[][] tabuleiro;
    private int turno = 1; // 1 = Jogador, 2 = Bot
    private int vencedor = 0;

    private int selR = -1, selC = -1; // Peça selecionada
    private List<Move> movimentosValidos;

    class Move {
        int r1, c1, r2, c2;
        boolean pulo;
        int jr, jc; // Coordenadas da peça comida

        public Move(int r1, int c1, int r2, int c2, boolean pulo, int jr, int jc) {
            this.r1 = r1; this.c1 = c1; this.r2 = r2; this.c2 = c2;
            this.pulo = pulo; this.jr = jr; this.jc = jc;
        }
    }

    public Damas() {
        setPreferredSize(new Dimension(LARGURA, ALTURA));
        setBackground(new Color(40, 45, 50));
        setFocusable(true);
        addMouseListener(this);
        addKeyListener(this);
        movimentosValidos = new ArrayList<>();
    }

    private void iniciarJogo() {
        tabuleiro = new int[8][8];
        for (int r = 0; r < 8; r++) {
            for (int c = 0; c < 8; c++) {
                if ((r + c) % 2 == 1) {
                    if (r < 3) tabuleiro[r][c] = 2; // Bot
                    else if (r > 4) tabuleiro[r][c] = 1; // Jogador
                }
            }
        }
        turno = 1;
        vencedor = 0;
        selR = -1; selC = -1;
        movimentosValidos.clear();
        estadoAtual = Estado.JOGANDO;
        repaint();
    }

    // --- LÓGICA DO JOGO ---

    private List<Move> getMovimentos(int jogador, int[][] b) {
        List<Move> moves = new ArrayList<>();
        boolean podePular = false;
        int dama = jogador == 1 ? 3 : 4;
        int direcaoNormal = jogador == 1 ? -1 : 1; // Jogador sobe (-1), Bot desce (+1)

        for (int r = 0; r < 8; r++) {
            for (int c = 0; c < 8; c++) {
                if (b[r][c] == jogador || b[r][c] == dama) {
                    boolean isDama = b[r][c] == dama;
                    int[] direcoesY = isDama ? new int[]{-1, 1} : new int[]{direcaoNormal};
                    
                    for (int dr : direcoesY) {
                        for (int dc : new int[]{-1, 1}) {
                            int nr = r + dr, nc = c + dc;
                            if (nr >= 0 && nr < 8 && nc >= 0 && nc < 8) {
                                // Movimento simples
                                if (b[nr][nc] == 0) {
                                    if (!podePular) moves.add(new Move(r, c, nr, nc, false, -1, -1));
                                } 
                                // Pulo (Captura)
                                else if (b[nr][nc] != jogador && b[nr][nc] != dama && b[nr][nc] != 0) {
                                    int jr = nr + dr, jc = nc + dc;
                                    if (jr >= 0 && jr < 8 && jc >= 0 && jc < 8 && b[jr][jc] == 0) {
                                        if (!podePular) {
                                            moves.clear(); // Obriga a capturar
                                            podePular = true;
                                        }
                                        moves.add(new Move(r, c, jr, jc, true, nr, nc));
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
        return moves;
    }

    private void aplicarMovimento(Move m, int[][] b) {
        int peca = b[m.r1][m.c1];
        b[m.r1][m.c1] = 0;
        b[m.r2][m.c2] = peca;
        if (m.pulo) b[m.jr][m.jc] = 0;

        // Promoção a Dama
        if (peca == 1 && m.r2 == 0) b[m.r2][m.c2] = 3;
        if (peca == 2 && m.r2 == 7) b[m.r2][m.c2] = 4;
    }

    private void verificarFimDeJogo() {
        if (getMovimentos(1, tabuleiro).isEmpty()) {
            vencedor = 2; estadoAtual = Estado.GAMEOVER;
        } else if (getMovimentos(2, tabuleiro).isEmpty()) {
            vencedor = 1; estadoAtual = Estado.GAMEOVER;
        }
    }

    // --- IA DO BOT (MINIMAX) ---

    private void jogadaBot() {
        new Thread(() -> {
            try { Thread.sleep(600); } catch (Exception e) {}
            
            Move melhorMove = getMelhorMove(dificuldade);
            if (melhorMove != null) {
                aplicarMovimento(melhorMove, tabuleiro);
                turno = 1;
                selR = -1; selC = -1;
                verificarFimDeJogo();
            } else {
                vencedor = 1; estadoAtual = Estado.GAMEOVER;
            }
            repaint();
        }).start();
    }

    private Move getMelhorMove(int diff) {
        int depth = diff == 1 ? 1 : (diff == 2 ? 3 : 5);
        List<Move> moves = getMovimentos(2, tabuleiro);
        if (moves.isEmpty()) return null;

        // Erro proposital no modo fácil
        if (diff == 1 && Math.random() < 0.4) {
            return moves.get((int) (Math.random() * moves.size()));
        }

        Move bestMove = null;
        int maxEval = Integer.MIN_VALUE;

        for (Move m : moves) {
            int[][] clone = clonarTabuleiro(tabuleiro);
            aplicarMovimento(m, clone);
            int eval = minimax(clone, depth - 1, false, Integer.MIN_VALUE, Integer.MAX_VALUE);
            if (eval > maxEval || bestMove == null) {
                maxEval = eval;
                bestMove = m;
            }
        }
        return bestMove;
    }

    private int minimax(int[][] b, int depth, boolean isMax, int alpha, int beta) {
        List<Move> moves = getMovimentos(isMax ? 2 : 1, b);
        if (depth == 0 || moves.isEmpty()) return avaliarTabuleiro(b);

        if (isMax) {
            int maxEval = Integer.MIN_VALUE;
            for (Move m : moves) {
                int[][] clone = clonarTabuleiro(b);
                aplicarMovimento(m, clone);
                int eval = minimax(clone, depth - 1, false, alpha, beta);
                maxEval = Math.max(maxEval, eval);
                alpha = Math.max(alpha, eval);
                if (beta <= alpha) break;
            }
            return maxEval;
        } else {
            int minEval = Integer.MAX_VALUE;
            for (Move m : moves) {
                int[][] clone = clonarTabuleiro(b);
                aplicarMovimento(m, clone);
                int eval = minimax(clone, depth - 1, true, alpha, beta);
                minEval = Math.min(minEval, eval);
                beta = Math.min(beta, eval);
                if (beta <= alpha) break;
            }
            return minEval;
        }
    }

    private int avaliarTabuleiro(int[][] b) {
        int score = 0;
        for (int r = 0; r < 8; r++) {
            for (int c = 0; c < 8; c++) {
                if (b[r][c] == 1) score -= 10;
                else if (b[r][c] == 3) score -= 30; // Dama jogador
                else if (b[r][c] == 2) score += 10;
                else if (b[r][c] == 4) score += 30; // Dama bot
            }
        }
        return score;
    }

    private int[][] clonarTabuleiro(int[][] b) {
        int[][] novo = new int[8][8];
        for (int i = 0; i < 8; i++) System.arraycopy(b[i], 0, novo[i], 0, 8);
        return novo;
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
            if (estadoAtual == Estado.GAMEOVER) desenharGameOver(g2d);
        }
    }

    private void desenharMenu(Graphics2D g) {
        g.setColor(Color.WHITE);
        g.setFont(new Font("Monospaced", Font.BOLD, 50));
        g.drawString("DAMAS vs BOT", LARGURA / 2 - 170, 180);

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
        // Textos
        g.setColor(Color.WHITE);
        g.setFont(new Font("Monospaced", Font.BOLD, 20));
        if (turno == 1) {
            g.setColor(Color.GREEN);
            g.drawString("SUA VEZ (Vermelho)", 20, 30);
        } else {
            g.setColor(Color.ORANGE);
            g.drawString("PENSANDO... (Preto)", 20, 30);
        }
        
        // Tabuleiro
        for (int r = 0; r < 8; r++) {
            for (int c = 0; c < 8; c++) {
                int px = OFFSET_X + c * TAM_CASA;
                int py = OFFSET_Y + r * TAM_CASA;
                
                if ((r + c) % 2 == 0) g.setColor(new Color(245, 222, 179)); // Claro
                else g.setColor(new Color(139, 69, 19)); // Escuro
                
                g.fillRect(px, py, TAM_CASA, TAM_CASA);

                // Destacar seleção
                if (r == selR && c == selC) {
                    g.setColor(new Color(255, 255, 0, 100));
                    g.fillRect(px, py, TAM_CASA, TAM_CASA);
                }

                // Peças
                int peca = tabuleiro[r][c];
                if (peca != 0) {
                    g.setColor(peca == 1 || peca == 3 ? new Color(200, 30, 30) : new Color(30, 30, 30));
                    g.fillOval(px + 8, py + 8, TAM_CASA - 16, TAM_CASA - 16);
                    g.setColor(Color.WHITE);
                    g.setStroke(new BasicStroke(2));
                    g.drawOval(px + 8, py + 8, TAM_CASA - 16, TAM_CASA - 16);

                    // Coroa para Damas
                    if (peca == 3 || peca == 4) {
                        g.setColor(Color.YELLOW);
                        g.fillOval(px + 22, py + 22, TAM_CASA - 44, TAM_CASA - 44);
                    }
                }
            }
        }

        // Dicas de movimento
        if (selR != -1) {
            g.setColor(new Color(0, 255, 0, 150));
            for (Move m : movimentosValidos) {
                if (m.r1 == selR && m.c1 == selC) {
                    g.fillOval(OFFSET_X + m.c2 * TAM_CASA + 20, OFFSET_Y + m.r2 * TAM_CASA + 20, 20, 20);
                }
            }
        }
    }

    private void desenharGameOver(Graphics2D g) {
        g.setColor(new Color(0, 0, 0, 180));
        g.fillRect(0, 0, LARGURA, ALTURA);

        g.setColor(vencedor == 1 ? Color.GREEN : Color.RED);
        g.setFont(new Font("Monospaced", Font.BOLD, 45));
        String msg = vencedor == 1 ? "VOCÊ VENCEU!" : "O BOT VENCEU!";
        g.drawString(msg, (LARGURA - g.getFontMetrics().stringWidth(msg)) / 2, ALTURA / 2 - 20);

        g.setColor(Color.WHITE);
        g.setFont(new Font("Monospaced", Font.PLAIN, 18));
        String recomecar = "Pressione ESPAÇO para reiniciar";
        g.drawString(recomecar, (LARGURA - g.getFontMetrics().stringWidth(recomecar)) / 2, ALTURA / 2 + 30);
    }

    // --- CONTROLES MOUSE ---

    @Override
    public void mouseReleased(MouseEvent e) {
        if (estadoAtual != Estado.JOGANDO || turno != 1) return;

        int mx = e.getX(), my = e.getY();
        if (mx < OFFSET_X || mx >= OFFSET_X + 8 * TAM_CASA || my < OFFSET_Y || my >= OFFSET_Y + 8 * TAM_CASA) return;

        int c = (mx - OFFSET_X) / TAM_CASA;
        int r = (my - OFFSET_Y) / TAM_CASA;

        List<Move> todosMoves = getMovimentos(1, tabuleiro);

        // Se clicou na própria peça
        if (tabuleiro[r][c] == 1 || tabuleiro[r][c] == 3) {
            selR = r; selC = c;
            movimentosValidos = todosMoves; // Atualiza dicas
            repaint();
            return;
        }

        // Se clicou num destino válido
        if (selR != -1 && tabuleiro[r][c] == 0) {
            for (Move m : todosMoves) {
                if (m.r1 == selR && m.c1 == selC && m.r2 == r && m.c2 == c) {
                    aplicarMovimento(m, tabuleiro);
                    selR = -1; selC = -1;
                    turno = 2; // Passa a vez
                    repaint();
                    verificarFimDeJogo();
                    if (estadoAtual == Estado.JOGANDO) jogadaBot();
                    return;
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
            if (key == KeyEvent.VK_ESCAPE) { estadoAtual = Estado.MENU; repaint(); }
        } else if (estadoAtual == Estado.GAMEOVER) {
            if (key == KeyEvent.VK_SPACE) iniciarJogo();
            if (key == KeyEvent.VK_ESCAPE) { estadoAtual = Estado.MENU; repaint(); }
        }
    }

    @Override public void keyReleased(KeyEvent e) {}
    @Override public void keyTyped(KeyEvent e) {}
}