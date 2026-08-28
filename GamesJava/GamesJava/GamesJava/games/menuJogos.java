import java.awt.*;
import java.awt.event.*;
import javax.swing.*;

public class menuJogos extends JFrame {

    private CardLayout cardLayout;
    private JPanel painelCards;

    public menuJogos() {
        setTitle("Central Arcade Java");
        setSize(1050, 800); 
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLocationRelativeTo(null); 
        setLayout(new BorderLayout());
        getContentPane().setBackground(new Color(25, 25, 25)); 

        // --- PAINEL DO TÍTULO E NAVEGAÇÃO ---
        JPanel painelTopo = new JPanel();
        painelTopo.setLayout(new BoxLayout(painelTopo, BoxLayout.Y_AXIS));
        painelTopo.setBackground(new Color(25, 25, 25));
        painelTopo.setBorder(BorderFactory.createEmptyBorder(20, 0, 10, 0));
        
        JLabel titulo = new JLabel("MINI GAMES JAVA");
        titulo.setForeground(Color.WHITE); 
        titulo.setFont(new Font("Monospaced", Font.BOLD, 45));
        titulo.setAlignmentX(Component.CENTER_ALIGNMENT);
        
        // Botões de Navegação (Categorias)
        JPanel painelNav = new JPanel(new FlowLayout(FlowLayout.CENTER, 30, 10));
        painelNav.setBackground(new Color(25, 25, 25));
        
        JButton btnFree = criarBotaoNav("PLANO FREE", new Color(40, 150, 80));
        JButton btnPro = criarBotaoNav("PLANO PRO", new Color(180, 140, 30));
        
        painelNav.add(btnFree);
        painelNav.add(btnPro);

        painelTopo.add(titulo);
        painelTopo.add(Box.createRigidArea(new Dimension(0, 15)));
        painelTopo.add(painelNav);

        // --- CONFIGURAÇÃO DAS PÁGINAS (CARDS) ---
        cardLayout = new CardLayout();
        painelCards = new JPanel(cardLayout);
        painelCards.setBackground(new Color(25, 25, 25));

        // Página 1: Plano Free
        JPanel paginaFree = criarPaginaFree();
        // Página 2: Plano Pro
        JPanel paginaPro = criarPaginaPro();

        painelCards.add(paginaFree, "FREE");
        painelCards.add(paginaPro, "PRO");

        // Ações dos botões de navegação
        btnFree.addActionListener(e -> cardLayout.show(painelCards, "FREE"));
        btnPro.addActionListener(e -> cardLayout.show(painelCards, "PRO"));

        // --- RODAPÉ ---
        JPanel painelRodape = new JPanel();
        painelRodape.setBackground(new Color(25, 25, 25));
        painelRodape.setBorder(BorderFactory.createEmptyBorder(10, 0, 30, 0));
        
        JButton btnSair = new JButton("SAIR DO PROGRAMA");
        btnSair.setFont(new Font("Monospaced", Font.BOLD, 16));
        btnSair.setBackground(new Color(200, 50, 50));
        btnSair.setForeground(Color.WHITE);
        btnSair.setFocusPainted(false);
        btnSair.setCursor(new Cursor(Cursor.HAND_CURSOR));
        btnSair.addActionListener(e -> System.exit(0));
        
        painelRodape.add(btnSair);

        // --- ADICIONANDO TUDO À JANELA ---
        add(painelTopo, BorderLayout.NORTH);
        add(painelCards, BorderLayout.CENTER);
        add(painelRodape, BorderLayout.SOUTH);
    }

    // Método auxiliar para criar os botões do menu superior
    private JButton criarBotaoNav(String texto, Color cor) {
        JButton btn = new JButton(texto);
        btn.setFont(new Font("Monospaced", Font.BOLD, 18));
        btn.setBackground(cor);
        btn.setForeground(Color.WHITE);
        btn.setFocusPainted(false);
        btn.setCursor(new Cursor(Cursor.HAND_CURSOR));
        btn.setPreferredSize(new Dimension(200, 40));
        return btn;
    }

    // --- PÁGINA: PLANO FREE ---
    private JPanel criarPaginaFree() {
        JPanel painelJogos = new JPanel();
        painelJogos.setBackground(new Color(25, 25, 25));
        painelJogos.setLayout(new GridLayout(0, 3, 25, 25)); // Grid que cresce linhas
        painelJogos.setBorder(BorderFactory.createEmptyBorder(10, 40, 20, 40)); 

        // 1. Criar Botões (1 a 13)
        BotaoCapa btnPong = new BotaoCapa("PONG", 1);
        BotaoCapa btnVelha = new BotaoCapa("VELHA", 2);
        BotaoCapa btnAsteroides = new BotaoCapa("ASTEROIDES", 3);
        BotaoCapa btnBreakout = new BotaoCapa("BREAKOUT", 4);
        BotaoCapa btnSnake = new BotaoCapa("SNAKE", 5);
        BotaoCapa btnTetris = new BotaoCapa("TETRIS", 6);
        BotaoCapa btnMinado = new BotaoCapa("CAMPO MINADO", 7);
        BotaoCapa btnSudoku = new BotaoCapa("SUDOKU", 8);
        BotaoCapa btnMemoria = new BotaoCapa("MEMÓRIA", 9); 
        BotaoCapa btnDino = new BotaoCapa("DINO RUNNER", 10); 
        BotaoCapa btnDamas = new BotaoCapa("DAMAS", 11);
        BotaoCapa btnSolitario = new BotaoCapa("SOLITÁRIO", 12);
        BotaoCapa btnBlackjack = new BotaoCapa("BLACKJACK", 13);

        // 2. Ações (Abrir cada jogo)
        btnPong.addActionListener(e -> abrirJogo(new Pong(), "Pong Arcade"));
        btnVelha.addActionListener(e -> abrirJogo(new JogodaVelha(), "Jogo da Velha"));
        btnAsteroides.addActionListener(e -> abrirJogo(new Asteroides(), "Asteroides Arcade"));
        btnBreakout.addActionListener(e -> abrirJogo(new Breakout(), "Breakout Arcade"));
        btnSnake.addActionListener(e -> abrirJogo(new Snake(), "Snake Arcade"));
        btnTetris.addActionListener(e -> abrirJogo(new Tetris(), "Tetris Arcade"));
        btnMinado.addActionListener(e -> abrirJogo(new CampoMinado(), "Campo Minado"));
        btnSudoku.addActionListener(e -> abrirJogo(new Sudoku(), "Sudoku Arcade"));
        btnMemoria.addActionListener(e -> abrirJogo(new JogoDaMemoria(), "Jogo da Memória"));
        btnDino.addActionListener(e -> abrirJogo(new DinoRunner(), "Dino Runner")); 
        btnDamas.addActionListener(e -> abrirJogo(new Damas(), "Jogo de Damas"));
        btnSolitario.addActionListener(e -> abrirJogo(new Solitario(), "Solitário (Paciência)"));
        btnBlackjack.addActionListener(e -> abrirJogo(new Blackjack(), "Blackjack 21"));

        // 3. Adicionar ao painel
        painelJogos.add(btnPong);
        painelJogos.add(btnVelha);
        painelJogos.add(btnAsteroides);
        painelJogos.add(btnBreakout);
        painelJogos.add(btnSnake);
        painelJogos.add(btnTetris);
        painelJogos.add(btnMinado);
        painelJogos.add(btnSudoku);
        painelJogos.add(btnMemoria); 
        painelJogos.add(btnDino);
        painelJogos.add(btnDamas);
        painelJogos.add(btnSolitario);
        painelJogos.add(btnBlackjack);

        // Wrapper para scroll
        JPanel wrapperJogos = new JPanel(new BorderLayout());
        wrapperJogos.setBackground(new Color(25, 25, 25));
        wrapperJogos.add(painelJogos, BorderLayout.NORTH);

        JScrollPane scrollPane = new JScrollPane(wrapperJogos);
        scrollPane.setBorder(null); 
        scrollPane.setHorizontalScrollBarPolicy(JScrollPane.HORIZONTAL_SCROLLBAR_NEVER);
        scrollPane.setVerticalScrollBarPolicy(JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED);
        scrollPane.getVerticalScrollBar().setUnitIncrement(16);
        scrollPane.setBackground(new Color(25, 25, 25));

        JPanel pagina = new JPanel(new BorderLayout());
        pagina.add(scrollPane, BorderLayout.CENTER);
        return pagina;
    }

    // --- PÁGINA: PLANO PRO ---
    private JPanel criarPaginaPro() {
        JPanel pagina = new JPanel(new GridBagLayout()); // Centraliza o conteúdo
        pagina.setBackground(new Color(25, 25, 25));
        
        JPanel container = new JPanel();
        container.setLayout(new BoxLayout(container, BoxLayout.Y_AXIS));
        container.setBackground(new Color(25, 25, 25));
        
        JLabel icone = new JLabel("⭐", SwingConstants.CENTER);
        icone.setFont(new Font("Arial", Font.PLAIN, 60));
        icone.setAlignmentX(Component.CENTER_ALIGNMENT);
        
        JLabel titulo = new JLabel("Área Exclusiva Pro");
        titulo.setForeground(new Color(200, 160, 40));
        titulo.setFont(new Font("Monospaced", Font.BOLD, 30));
        titulo.setAlignmentX(Component.CENTER_ALIGNMENT);
        
        JLabel subtitulo = new JLabel("Nenhum jogo disponível nesta categoria ainda.");
        subtitulo.setForeground(Color.GRAY);
        subtitulo.setFont(new Font("Monospaced", Font.PLAIN, 18));
        subtitulo.setAlignmentX(Component.CENTER_ALIGNMENT);
        
        container.add(icone);
        container.add(Box.createRigidArea(new Dimension(0, 20)));
        container.add(titulo);
        container.add(Box.createRigidArea(new Dimension(0, 15)));
        container.add(subtitulo);
        
        pagina.add(container);
        return pagina;
    }

    private void abrirJogo(JPanel painelDoJogo, String tituloJanela) {
        this.setVisible(false);
        JFrame frameJogo = new JFrame(tituloJanela);
        frameJogo.add(painelDoJogo);
        frameJogo.pack(); 
        frameJogo.setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
        frameJogo.setResizable(false);
        frameJogo.setLocationRelativeTo(null); 
        frameJogo.setVisible(true);

        frameJogo.addWindowListener(new WindowAdapter() {
            @Override
            public void windowClosed(WindowEvent e) {
                menuJogos.this.setVisible(true); 
            }
        });
    }

    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> {
            menuJogos menu = new menuJogos();
            menu.setVisible(true);
        });
    }
}

// --- CLASSE DO BOTÃO DE CAPA (MANTIDA INTACTA) ---
class BotaoCapa extends JButton {
    private String titulo;
    private int tipoJogo;
    private Color corFundo = new Color(45, 45, 50);
    private Color corHover = new Color(60, 60, 70);
    private boolean mouseEmCima = false;

    public BotaoCapa(String titulo, int tipoJogo) {
        this.titulo = titulo;
        this.tipoJogo = tipoJogo;
        
        setPreferredSize(new Dimension(0, 260)); 
        setCursor(new Cursor(Cursor.HAND_CURSOR));
        setContentAreaFilled(false);
        setBorderPainted(false);

        addMouseListener(new MouseAdapter() {
            public void mouseEntered(MouseEvent e) { mouseEmCima = true; repaint(); }
            public void mouseExited(MouseEvent e) { mouseEmCima = false; repaint(); }
        });
    }

    @Override
    protected void paintComponent(Graphics g) {
        Graphics2D g2d = (Graphics2D) g;
        g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

        // Fundo e Borda
        g2d.setColor(mouseEmCima ? corHover : corFundo);
        g2d.fillRoundRect(0, 0, getWidth(), getHeight(), 20, 20);
        g2d.setColor(mouseEmCima ? Color.WHITE : new Color(100, 100, 100));
        g2d.setStroke(new BasicStroke(3));
        g2d.drawRoundRect(1, 1, getWidth() - 3, getHeight() - 3, 20, 20);

        // Chamar o desenho da arte de cada jogo
        if (tipoJogo == 1) desenharArtePong(g2d);
        else if (tipoJogo == 2) desenharArteVelha(g2d);
        else if (tipoJogo == 3) desenharArteAsteroides(g2d);
        else if (tipoJogo == 4) desenharArteBreakout(g2d);
        else if (tipoJogo == 5) desenharArteSnake(g2d);
        else if (tipoJogo == 6) desenharArteTetris(g2d);
        else if (tipoJogo == 7) desenharArteCampoMinado(g2d);
        else if (tipoJogo == 8) desenharArteSudoku(g2d);
        else if (tipoJogo == 9) desenharArteMemoria(g2d); 
        else if (tipoJogo == 10) desenharArteDino(g2d);
        else if (tipoJogo == 11) desenharArteDamas(g2d); 
        else if (tipoJogo == 12) desenharArteSolitario(g2d); 
        else if (tipoJogo == 13) desenharArteBlackjack(g2d);

        // Barra inferior com o Nome do Jogo
        g2d.setColor(new Color(15, 15, 15, 200));
        g2d.fillRoundRect(0, getHeight() - 50, getWidth(), 50, 20, 20);
        g2d.fillRect(0, getHeight() - 60, getWidth(), 20); // conserta cantos

        g2d.setColor(Color.WHITE);
        g2d.setFont(new Font("Monospaced", Font.BOLD, 18));
        FontMetrics metrics = g2d.getFontMetrics();
        int xTexto = (getWidth() - metrics.stringWidth(titulo)) / 2;
        g2d.drawString(titulo, xTexto, getHeight() - 20);
    }

    // --- ARTES DOS JOGOS ---
    private void desenharArtePong(Graphics2D g) {
        int cx = getWidth() / 2;
        int cy = getHeight() / 2 - 20;
        g.setColor(Color.WHITE);
        g.fillRect(cx - 60, cy - 30, 10, 60); 
        g.fillRect(cx + 50, cy - 30, 10, 60); 
        g.fillOval(cx - 15, cy - 10, 20, 20); 
        for(int i = cy - 60; i < cy + 60; i += 15) g.fillRect(cx - 2, i, 4, 10);
    }

    private void desenharArteVelha(Graphics2D g) {
        int cx = getWidth() / 2, cy = getHeight() / 2 - 20;
        g.setColor(Color.WHITE); g.setStroke(new BasicStroke(5));
        g.drawLine(cx - 20, cy - 60, cx - 20, cy + 60);
        g.drawLine(cx + 20, cy - 60, cx + 20, cy + 60);
        g.drawLine(cx - 60, cy - 20, cx + 60, cy - 20);
        g.drawLine(cx - 60, cy + 20, cx + 60, cy + 20);
        g.setColor(Color.RED);
        g.drawLine(cx - 50, cy - 50, cx - 30, cy - 30);
        g.drawLine(cx - 30, cy - 50, cx - 50, cy - 30);
        g.setColor(Color.BLUE);
        g.drawOval(cx + 30, cy + 30, 20, 20);
    }

    private void desenharArteAsteroides(Graphics2D g) {
        int cx = getWidth() / 2, cy = getHeight() / 2 - 20;
        g.setColor(Color.WHITE);
        int[] nx = {cx, cx - 15, cx + 15};
        int[] ny = {cy - 20, cy + 20, cy + 20};
        g.drawPolygon(nx, ny, 3);
        g.drawOval(cx - 50, cy - 40, 20, 20);
        g.drawOval(cx + 30, cy - 10, 30, 25);
    }

    private void desenharArteBreakout(Graphics2D g) {
        int cx = getWidth() / 2, cy = getHeight() / 2 - 20;
        Color[] cores = {Color.RED, Color.ORANGE, Color.GREEN};
        for(int row=0; row<3; row++) {
            g.setColor(cores[row]);
            g.fillRect(cx - 60, cy - 50 + (row*15), 35, 10);
            g.fillRect(cx - 20, cy - 50 + (row*15), 35, 10);
            g.fillRect(cx + 20, cy - 50 + (row*15), 35, 10);
        }
        g.setColor(Color.WHITE); g.fillOval(cx - 5, cy + 10, 10, 10);
        g.setColor(Color.CYAN); g.fillRect(cx - 30, cy + 50, 60, 10);
    }

    private void desenharArteSnake(Graphics2D g) {
        int cx = getWidth() / 2, cy = getHeight() / 2 - 20;
        g.setColor(Color.GREEN);
        g.fillRect(cx - 40, cy, 20, 20);
        g.fillRect(cx - 20, cy, 20, 20);
        g.fillRect(cx, cy, 20, 20);
        g.fillRect(cx, cy - 20, 20, 20);
        g.setColor(Color.RED); g.fillOval(cx + 30, cy - 20, 15, 15);
    }

    private void desenharArteTetris(Graphics2D g) {
        int cx = getWidth() / 2, cy = getHeight() / 2 - 20, t = 20;
        g.setColor(Color.MAGENTA); 
        g.fillRect(cx - t, cy, t, t);
        g.fillRect(cx, cy, t, t);
        g.fillRect(cx + t, cy, t, t);
        g.fillRect(cx, cy - t, t, t);
    }

    private void desenharArteCampoMinado(Graphics2D g) {
        int cx = getWidth() / 2, cy = getHeight() / 2 - 20;
        g.setColor(Color.LIGHT_GRAY);
        g.fillRect(cx - 45, cy - 45, 30, 30);
        g.fillRect(cx - 15, cy - 45, 30, 30);
        g.fillRect(cx + 15, cy - 45, 30, 30);
        g.fillRect(cx - 45, cy - 15, 30, 30);
        g.setColor(Color.DARK_GRAY); g.fillRect(cx - 15, cy - 15, 30, 30);
        g.setColor(Color.RED); g.fillOval(cx - 10, cy - 10, 20, 20);
    }

    private void desenharArteSudoku(Graphics2D g) {
        int cx = getWidth() / 2, cy = getHeight() / 2 - 20;
        g.setColor(Color.WHITE); g.setStroke(new BasicStroke(2));
        g.drawRect(cx - 40, cy - 40, 80, 80);
        g.drawLine(cx - 13, cy - 40, cx - 13, cy + 40);
        g.drawLine(cx + 14, cy - 40, cx + 14, cy + 40);
        g.drawLine(cx - 40, cy - 13, cx + 40, cy - 13);
        g.drawLine(cx - 40, cy + 14, cx + 40, cy + 14);
        g.setFont(new Font("Arial", Font.BOLD, 20));
        g.drawString("5", cx - 30, cy + 5);
        g.drawString("3", cx + 20, cy + 30);
    }

    private void desenharArteMemoria(Graphics2D g) {
        int cx = getWidth() / 2, cy = getHeight() / 2 - 20;
        g.setColor(Color.WHITE);
        g.fillRoundRect(cx - 40, cy - 30, 30, 40, 5, 5);
        g.fillRoundRect(cx + 10, cy - 30, 30, 40, 5, 5);
        g.setColor(Color.RED); g.setFont(new Font("Arial", Font.BOLD, 20));
        g.drawString("?", cx - 32, cy - 2); g.drawString("?", cx + 18, cy - 2);
    }

    private void desenharArteDino(Graphics2D g) {
        int cx = getWidth() / 2, cy = getHeight() / 2 - 10;
        g.setColor(Color.WHITE); g.drawLine(cx - 50, cy + 30, cx + 50, cy + 30);
        g.fillRect(cx - 30, cy, 20, 20); 
        g.fillRect(cx - 15, cy - 15, 15, 15); 
        g.setColor(Color.GREEN); g.fillRect(cx + 20, cy + 10, 10, 20);
    }

    private void desenharArteDamas(Graphics2D g) {
        int tam = 16, cx = getWidth() / 2 - (tam * 2), cy = getHeight() / 2 - 45;
        for (int r = 0; r < 4; r++) {
            for (int c = 0; c < 4; c++) {
                g.setColor((r + c) % 2 == 1 ? new Color(139, 69, 19) : new Color(245, 222, 179));
                g.fillRect(cx + c * tam, cy + r * tam, tam, tam);
            }
        }
        g.setColor(new Color(200, 30, 30)); g.fillOval(cx + tam + 2, cy + 2, tam - 4, tam - 4);
        g.setColor(new Color(30, 30, 30)); g.fillOval(cx + 2, cy + 3 * tam + 2, tam - 4, tam - 4);
    }

    private void desenharArteSolitario(Graphics2D g) {
        int cx = getWidth() / 2, cy = getHeight() / 2 - 20;
        g.setColor(Color.WHITE); g.fillRoundRect(cx - 30, cy - 40, 40, 60, 8, 8);
        g.setColor(Color.BLACK); g.drawRoundRect(cx - 30, cy - 40, 40, 60, 8, 8);
        g.setFont(new Font("Arial", Font.BOLD, 12)); g.drawString("A♠", cx - 25, cy - 20);
        g.setColor(Color.WHITE); g.fillRoundRect(cx - 10, cy - 20, 40, 60, 8, 8);
        g.setColor(Color.BLACK); g.drawRoundRect(cx - 10, cy - 20, 40, 60, 8, 8);
        g.setColor(Color.RED); g.drawString("K♥", cx - 5, cy);
    }

    private void desenharArteBlackjack(Graphics2D g) {
        int cx = getWidth() / 2, cy = getHeight() / 2 - 20;
        g.setColor(Color.RED); g.fillOval(cx - 40, cy + 10, 30, 30);
        g.setColor(Color.WHITE); g.drawOval(cx - 35, cy + 15, 20, 20);
        g.setColor(Color.WHITE);
        g.fillRoundRect(cx, cy - 40, 35, 55, 5, 5);
        g.fillRoundRect(cx + 15, cy - 30, 35, 55, 5, 5);
        g.setColor(Color.BLACK);
        g.drawRoundRect(cx, cy - 40, 35, 55, 5, 5);
        g.drawRoundRect(cx + 15, cy - 30, 35, 55, 5, 5);
        g.setFont(new Font("Arial", Font.BOLD, 14));
        g.drawString("J♠", cx + 5, cy - 20);
        g.setColor(Color.RED); g.drawString("A♥", cx + 20, cy - 10);
    }
}