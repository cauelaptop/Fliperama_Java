import java.awt.*;
import java.awt.event.*;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import javax.swing.*;

public class Solitario extends JPanel implements MouseListener, MouseMotionListener, KeyListener {
    private static final int LARGURA = 800;
    private static final int ALTURA = 600;
    private static final int CARTA_L = 70;
    private static final int CARTA_A = 100;

    private List<Carta> baralho;
    private List<Carta> monte;
    private List<Carta> descarte;
    private List<List<Carta>> fundacoes;
    private List<List<Carta>> colunas;

    // Sistema de Drag & Drop
    private List<Carta> cartasArrastadas;
    private Object origemArraste; // Pode ser 'descarte', uma lista de 'fundacoes' ou 'colunas'
    private int offsetX, offsetY;
    private int mouseX, mouseY;

    private boolean venceu = false;

    class Carta {
        int valor; // 1 (Ás) a 13 (Rei)
        int naipe; // 0=Copas, 1=Espadas, 2=Ouros, 3=Paus
        boolean viradaPraCima;
        int x, y; // Posição para renderização e clique

        public Carta(int valor, int naipe) {
            this.valor = valor;
            this.naipe = naipe;
            this.viradaPraCima = false;
        }

        public boolean isVermelha() {
            return naipe == 0 || naipe == 2;
        }

        public String getSimboloValor() {
            if (valor == 1) return "A";
            if (valor == 11) return "J";
            if (valor == 12) return "Q";
            if (valor == 13) return "K";
            return String.valueOf(valor);
        }

        public String getSimboloNaipe() {
            if (naipe == 0) return "♥";
            if (naipe == 1) return "♠";
            if (naipe == 2) return "♦";
            return "♣";
        }

        public Color getCor() {
            return isVermelha() ? Color.RED : Color.BLACK;
        }
    }

    public Solitario() {
        setPreferredSize(new Dimension(LARGURA, ALTURA));
        setBackground(new Color(0, 128, 0)); // Verde mesa de feltro
        setFocusable(true);
        addMouseListener(this);
        addMouseMotionListener(this);
        addKeyListener(this);
        iniciarJogo();
    }

    private void iniciarJogo() {
        baralho = new ArrayList<>();
        for (int naipe = 0; naipe < 4; naipe++) {
            for (int valor = 1; valor <= 13; valor++) {
                baralho.add(new Carta(valor, naipe));
            }
        }
        Collections.shuffle(baralho);

        colunas = new ArrayList<>();
        for (int i = 0; i < 7; i++) colunas.add(new ArrayList<>());
        fundacoes = new ArrayList<>();
        for (int i = 0; i < 4; i++) fundacoes.add(new ArrayList<>());
        monte = new ArrayList<>();
        descarte = new ArrayList<>();
        cartasArrastadas = new ArrayList<>();
        venceu = false;

        // Distribuir cartas nas colunas (Tableau)
        int cartaIndex = 0;
        for (int i = 0; i < 7; i++) {
            for (int j = 0; j <= i; j++) {
                Carta c = baralho.get(cartaIndex++);
                if (j == i) c.viradaPraCima = true; // Só a última fica virada
                colunas.get(i).add(c);
            }
        }

        // Resto vai pro monte
        while (cartaIndex < 52) {
            monte.add(baralho.get(cartaIndex++));
        }

        posicionarCartas();
        repaint();
    }

    private void posicionarCartas() {
        // Monte
        for (Carta c : monte) { c.x = 20; c.y = 20; }
        // Descarte
        for (Carta c : descarte) { c.x = 100; c.y = 20; }
        // Fundações
        for (int i = 0; i < 4; i++) {
            for (Carta c : fundacoes.get(i)) { c.x = 340 + (i * 90); c.y = 20; }
        }
        // Colunas
        for (int i = 0; i < 7; i++) {
            List<Carta> col = colunas.get(i);
            for (int j = 0; j < col.size(); j++) {
                Carta c = col.get(j);
                c.x = 20 + (i * 110);
                c.y = 150 + (j * 25);
            }
        }
    }

    private void checarVitoria() {
        for (List<Carta> f : fundacoes) {
            if (f.size() < 13) return;
        }
        venceu = true;
    }

    @Override
    protected void paintComponent(Graphics g) {
        super.paintComponent(g);
        Graphics2D g2d = (Graphics2D) g;
        g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

        // Desenhar espaços vazios
        g.setColor(new Color(0, 100, 0));
        g.drawRoundRect(20, 20, CARTA_L, CARTA_A, 10, 10); // Monte
        g.drawRoundRect(100, 20, CARTA_L, CARTA_A, 10, 10); // Descarte
        for (int i = 0; i < 4; i++) g.drawRoundRect(340 + (i * 90), 20, CARTA_L, CARTA_A, 10, 10); // Fundações
        for (int i = 0; i < 7; i++) g.drawRoundRect(20 + (i * 110), 150, CARTA_L, CARTA_A, 10, 10); // Colunas

        // Se o monte tá vazio, desenhar um O de reciclar
        if (monte.isEmpty()) {
            g.setColor(new Color(0, 160, 0));
            g.setFont(new Font("Arial", Font.BOLD, 40));
            g.drawString("⟳", 40, 80);
        }

        // Desenhar cartas paradas
        if (!monte.isEmpty()) desenharCarta(g2d, monte.get(monte.size() - 1));
        if (!descarte.isEmpty()) desenharCarta(g2d, descarte.get(descarte.size() - 1));
        
        for (List<Carta> f : fundacoes) {
            if (!f.isEmpty()) desenharCarta(g2d, f.get(f.size() - 1));
        }

        for (List<Carta> col : colunas) {
            for (Carta c : col) {
                desenharCarta(g2d, c);
            }
        }

        // Desenhar cartas arrastadas por cima de tudo
        for (Carta c : cartasArrastadas) {
            desenharCarta(g2d, c);
        }

        if (venceu) {
            g.setColor(new Color(0, 0, 0, 150));
            g.fillRect(0, 0, LARGURA, ALTURA);
            g.setColor(Color.YELLOW);
            g.setFont(new Font("Monospaced", Font.BOLD, 50));
            g.drawString("VOCÊ VENCEU!", LARGURA / 2 - 160, ALTURA / 2);
            g.setFont(new Font("Monospaced", Font.PLAIN, 20));
            g.setColor(Color.WHITE);
            g.drawString("Pressione ESPAÇO para jogar novamente", LARGURA / 2 - 200, ALTURA / 2 + 40);
        }
        
        g.setColor(Color.WHITE);
        g.setFont(new Font("Monospaced", Font.PLAIN, 12));
        g.drawString("[ ESC ] VOLTAR", 10, 580);
    }

    private void desenharCarta(Graphics2D g, Carta c) {
        g.setColor(Color.WHITE);
        g.fillRoundRect(c.x, c.y, CARTA_L, CARTA_A, 10, 10);
        g.setColor(Color.BLACK);
        g.drawRoundRect(c.x, c.y, CARTA_L, CARTA_A, 10, 10);

        if (!c.viradaPraCima) {
            // Fundo da carta
            g.setColor(new Color(30, 80, 150));
            g.fillRoundRect(c.x + 4, c.y + 4, CARTA_L - 8, CARTA_A - 8, 6, 6);
            g.setColor(Color.WHITE);
            g.drawRoundRect(c.x + 8, c.y + 8, CARTA_L - 16, CARTA_A - 16, 4, 4);
        } else {
            // Frente da carta
            g.setColor(c.getCor());
            g.setFont(new Font("Arial", Font.BOLD, 18));
            g.drawString(c.getSimboloValor(), c.x + 5, c.y + 20);
            g.drawString(c.getSimboloNaipe(), c.x + 5, c.y + 40);
            
            // Simbolo grande invertido em baixo (opcional, simplificado)
            g.setFont(new Font("Arial", Font.BOLD, 30));
            g.drawString(c.getSimboloNaipe(), c.x + 20, c.y + 65);
        }
    }

    private boolean colidiuCartas(int mx, int my, int cx, int cy) {
        return mx >= cx && mx <= cx + CARTA_L && my >= cy && my <= cy + CARTA_A;
    }

    @Override
    public void mousePressed(MouseEvent e) {
        if (venceu) return;
        int mx = e.getX();
        int my = e.getY();
        mouseX = mx; mouseY = my;

        // 1. Clicou no Monte?
        if (colidiuCartas(mx, my, 20, 20)) {
            if (!monte.isEmpty()) {
                Carta c = monte.remove(monte.size() - 1);
                c.viradaPraCima = true;
                descarte.add(c);
            } else {
                // Reciclar descarte pro monte
                while (!descarte.isEmpty()) {
                    Carta c = descarte.remove(descarte.size() - 1);
                    c.viradaPraCima = false;
                    monte.add(c);
                }
            }
            posicionarCartas();
            repaint();
            return;
        }

        // 2. Clicou no Descarte? (Pegar topo)
        if (!descarte.isEmpty() && colidiuCartas(mx, my, 100, 20)) {
            iniciarArraste(descarte, descarte.size() - 1, mx, my);
            return;
        }

        // 3. Clicou numa Fundação? (Pegar topo)
        for (int i = 0; i < 4; i++) {
            List<Carta> f = fundacoes.get(i);
            if (!f.isEmpty() && colidiuCartas(mx, my, f.get(f.size() - 1).x, f.get(f.size() - 1).y)) {
                iniciarArraste(f, f.size() - 1, mx, my);
                return;
            }
        }

        // 4. Clicou no Tableau (Colunas)
        for (int i = 0; i < 7; i++) {
            List<Carta> col = colunas.get(i);
            // Procura de cima pra baixo (do topo da tela pra baixo)
            for (int j = col.size() - 1; j >= 0; j--) {
                Carta c = col.get(j);
                if (colidiuCartas(mx, my, c.x, c.y)) {
                    if (!c.viradaPraCima) {
                        // Se clicou numa carta virada pra baixo, só vira se for a última
                        if (j == col.size() - 1) {
                            c.viradaPraCima = true;
                            repaint();
                        }
                    } else {
                        // Iniciar arraste dessa carta e todas as debaixo dela
                        iniciarArraste(col, j, mx, my);
                    }
                    return;
                }
            }
        }
    }

    private void iniciarArraste(List<Carta> origem, int index, int mx, int my) {
        origemArraste = origem;
        offsetX = mx - origem.get(index).x;
        offsetY = my - origem.get(index).y;

        // Remove da origem e bota na lista de arrastados
        while (origem.size() > index) {
            cartasArrastadas.add(origem.remove(index));
        }
    }

    @Override
    public void mouseDragged(MouseEvent e) {
        if (!cartasArrastadas.isEmpty()) {
            int dx = e.getX() - mouseX;
            int dy = e.getY() - mouseY;
            
            for (Carta c : cartasArrastadas) {
                c.x += dx;
                c.y += dy;
            }
            
            mouseX = e.getX();
            mouseY = e.getY();
            repaint();
        }
    }

    @Override
    @SuppressWarnings("unchecked")
    public void mouseReleased(MouseEvent e) {
        if (cartasArrastadas.isEmpty()) return;

        Carta baseArraste = cartasArrastadas.get(0);
        int mx = e.getX();
        int my = e.getY();
        boolean soltouValido = false;

        // Tentar soltar na Fundação (só permite se for 1 carta arrastada)
        if (cartasArrastadas.size() == 1) {
            for (int i = 0; i < 4; i++) {
                int fx = 340 + (i * 90);
                int fy = 20;
                if (colidiuCartas(mx, my, fx, fy) || (!fundacoes.get(i).isEmpty() && colidiuCartas(mx, my, fundacoes.get(i).get(fundacoes.get(i).size()-1).x, fundacoes.get(i).get(fundacoes.get(i).size()-1).y))) {
                    List<Carta> f = fundacoes.get(i);
                    if (f.isEmpty() && baseArraste.valor == 1) {
                        f.add(cartasArrastadas.remove(0));
                        soltouValido = true;
                        break;
                    } else if (!f.isEmpty()) {
                        Carta topoF = f.get(f.size() - 1);
                        if (topoF.naipe == baseArraste.naipe && topoF.valor == baseArraste.valor - 1) {
                            f.add(cartasArrastadas.remove(0));
                            soltouValido = true;
                            break;
                        }
                    }
                }
            }
        }

        // Tentar soltar no Tableau (Colunas)
        if (!soltouValido) {
            for (int i = 0; i < 7; i++) {
                List<Carta> col = colunas.get(i);
                int cx = 20 + (i * 110);
                int cy = col.isEmpty() ? 150 : col.get(col.size() - 1).y;

                if (colidiuCartas(mx, my, cx, cy) || colidiuCartas(baseArraste.x, baseArraste.y, cx, cy)) {
                    if (col.isEmpty() && baseArraste.valor == 13) { // Só Rei na coluna vazia
                        col.addAll(cartasArrastadas);
                        cartasArrastadas.clear();
                        soltouValido = true;
                        break;
                    } else if (!col.isEmpty()) {
                        Carta topoCol = col.get(col.size() - 1);
                        if (topoCol.isVermelha() != baseArraste.isVermelha() && topoCol.valor == baseArraste.valor + 1) {
                            col.addAll(cartasArrastadas);
                            cartasArrastadas.clear();
                            soltouValido = true;
                            break;
                        }
                    }
                }
            }
        }

        // Se soltou num lugar inválido, devolve pra origem
        if (!soltouValido && origemArraste != null) {
            ((List<Carta>) origemArraste).addAll(cartasArrastadas);
            cartasArrastadas.clear();
        }

        origemArraste = null;
        posicionarCartas();
        checarVitoria();
        repaint();
    }

    @Override
    public void keyPressed(KeyEvent e) {
        if (e.getKeyCode() == KeyEvent.VK_ESCAPE) {
            JFrame frame = (JFrame) SwingUtilities.getWindowAncestor(this);
            if (frame != null) frame.dispose();
        } else if (e.getKeyCode() == KeyEvent.VK_SPACE && venceu) {
            iniciarJogo();
        }
    }

    @Override public void keyReleased(KeyEvent e) {}
    @Override public void keyTyped(KeyEvent e) {}
    @Override public void mouseClicked(MouseEvent e) {}
    @Override public void mouseEntered(MouseEvent e) {}
    @Override public void mouseExited(MouseEvent e) {}
    @Override public void mouseMoved(MouseEvent e) {}
}