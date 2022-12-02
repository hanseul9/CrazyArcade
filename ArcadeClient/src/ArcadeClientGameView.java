
// JavaObjClientView.java ObjecStram 기반 Client
//실질적인 채팅 창
import java.awt.BorderLayout;
import java.awt.EventQueue;
import java.awt.FileDialog;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.FocusEvent;
import java.awt.event.FocusListener;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.awt.event.MouseMotionListener;
import java.awt.event.MouseWheelEvent;
import java.awt.event.MouseWheelListener;
import java.awt.image.ImageObserver;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.OutputStream;
import java.io.UnsupportedEncodingException;
import java.net.Socket;
import java.util.Random;
import java.util.Vector;
import javax.sound.sampled.AudioInputStream;
import javax.sound.sampled.AudioSystem;
import javax.sound.sampled.Clip;
import javax.sound.sampled.FloatControl;
import javax.swing.JFrame;
import javax.swing.JPanel;
import javax.swing.border.EmptyBorder;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;
import javax.swing.JTextField;
import javax.swing.JTextPane;
import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.SwingConstants;
import java.awt.Font;
import java.awt.Frame;
import java.awt.Graphics;
import java.awt.Image;
import java.awt.MediaTracker;
import java.awt.Toolkit;
import java.awt.Color;
import javax.swing.border.BevelBorder;
import javax.swing.border.LineBorder;
import javax.swing.JToggleButton;
import javax.swing.JList;
import java.awt.Canvas;
import javax.swing.border.TitledBorder;
import javax.swing.text.BadLocationException;
import javax.swing.text.SimpleAttributeSet;
import javax.swing.text.StyleConstants;
import javax.swing.text.StyledDocument;

public class ArcadeClientGameView extends JFrame implements FocusListener, KeyListener, Runnable{
	
	ArcadeClientGameView gameView = this;
	
	//기본 윈도우를 형성하는 프레임을 만든다
	//KeyListener : 키보드 입력 이벤트를 받는다
	//Runnable : 스레드를 가능하게 한다
	
	// ----------------- 1p이동 -----------------
	public static final int UP_PRESSED = 0x001;
    public static final int DOWN_PRESSED = 0x002;
    public static final int LEFT_PRESSED = 0x004;
    public static final int RIGHT_PRESSED = 0x008;
    
 // ----------------- 2p이동 -----------------
 // P2
 	public final static int UP_PRESSED2		=0x001;
 	public final static int DOWN_PRESSED2	=0x002;
 	public final static int LEFT_PRESSED2	=0x004;
 	public final static int RIGHT_PRESSED2	=0x008;
 	
    
    //버블 뜬금없이 생겼던게 조합따라 스페이스바 눌린걸로 판단됐던거같음 << 아하!
    //그래서 20으로 확 올려줌
    public static final int BUBBLE_PRESSED = 0x020;
    
    public final static int BUBBLE_PRESSED2	=0x020;

    GameScreen gamescreen;//Canvas 객체를 상속한 화면 묘화 메인 클래스
    
    Thread mainwork;//스레드 객체
	boolean roof=true;//스레드 루프 정보
	//Random rnd = new Random();	 // 랜덤 선언
	
	int bubbleCnt = 4; //버블 싱크

    
    //게임 제어를 위한 변수	
    int status; // 게임의 상태. 2:playing 4: 게임오버 
	int cnt;//루프 제어용 컨트롤 변수
	int delay;//루프 딜레이. 1/1000초 단위.
	long pretime;//루프 간격을 조절하기 위한 시간 체크값
	
	int key=0;
	int keyR=0;
	int key2=0;
	int keyR2=0;
	int keybuff;//1p키 버퍼값
	int keybuff2;//2p키 버퍼값
	

    
	
	//게임용 변수
	//int score;//점수
	//int mylife;//남은 목숨 <<할 수 있다면 이걸 바늘로...?
	int gamecnt;//게임 흐름 컨트롤
	int scrspeed=16;//스크롤 속도
	//int level;//게임 레벨
	
	
	int bkimg; //block1...8 블록이미지
	int bkx,bky;//타일블록 위치
	
	// 0 - 빈블럭, 1 - 박스, 2 - 빨강블럭, 3 - 주황블럭, 4 - 주황집
	// 5 - 노랑집, 6 - 파랑집, 7 - 나무, 8 - 풀
	// -1 : 생성된지 얼마 안 된 물풍선
	// 10 : 생성된지 시간이 좀 지난 물풍선
	
//	   int[][] MapArray = { //맵
//			   {0, 3, 2, 3, 2, 8, 0, 0, 1, 8, 5, 2, 5, 0, 5}, 
//			   {0, 4, 1, 4, 1, 7, 1, 0, 0, 7, 2, 3, 0, 0, 1}, 
//			   {0, 0, 3, 2, 3, 8, 0, 1, 1, 8, 5, 1, 5, 1, 5},
//			   {1, 4, 1, 4, 1, 7, 1, 0, 0, 7, 3, 2, 3, 2, 3},
//			   {2, 3, 2, 3, 2, 8, 0, 0, 1, 8, 5, 1, 5, 1, 5},
//			   {3, 4, 3, 4, 3, 7, 1, 1, 0, 0, 2, 3, 2, 3, 2},
//			   {7, 8, 7, 8, 7, 8, 0, 0, 1, 8, 7, 8, 7, 8, 7},
//			   {2, 3, 2, 3, 2, 0, 1, 0, 0, 7, 2, 4, 2, 4, 2},
//			   {6, 1, 6, 1, 6, 8, 0, 1, 1, 8, 3, 2, 3, 2, 3},
//			   {3, 2, 3, 2, 3, 7, 1, 0, 0, 7, 1, 4, 1, 4, 1},
//			   {6, 0, 6, 1, 6, 8, 0, 0, 1, 8, 2, 3, 2, 3, 0},
//			   {0, 0, 2, 3, 2, 7, 1, 1, 0, 7, 1, 4, 1, 4, 0},
//			   {6, 0, 6, 2, 6, 8, 0, 0, 1, 8, 3, 2, 3, 0, 0},
//		                   };
	   
		int[][] MapArray = { //테스트맵
				{0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0}, 
				{0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 1, 0, 0, 0},
				{0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 1, 0, 0, 0},
				{0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 3, 3, 0, 0}, 
				{0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 1, 3, 0, 0},
				{0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 1, 1, 0, 0},
				{0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 1, 1, 0, 0}, 
				{0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 1, 1, 0, 0},
				{0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 1, 0, 0, 0},
				{0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 1, 0, 0, 0}, 
				{0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 1, 0, 0, 0},
				{0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 1, 0, 0, 0},
				{0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0}
		 					};
	
		int[][] ItemArray = { // 아이템 위치
				//1 : 스피드      2 : 물줄기      3: 풀풍선 최대 개수
				{1, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 1, 0},
				{0, 0, 1, 0, 0, 0, 0, 0, 0, 0, 0, 1, 0, 0, 0},
				{0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 2, 0, 0, 0},
				{0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 3, 0, 0, 0},
				{0, 0, 0, 1, 0, 0, 0, 0, 0, 0, 0, 2, 0, 0, 0},
				{0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 1, 0, 0, 0},
				{0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 2, 0, 0, 0},
				{0, 3, 3, 3, 3, 3, 3, 3, 0, 0, 0, 3, 0, 0, 0},
				{0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 2, 0, 0, 0},
				{0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 1, 0, 0, 0},
				{0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 2, 0, 0, 0},
				{0, 2, 2, 2, 2, 0, 0, 0, 0, 0, 0, 3, 0, 0, 0},
				{0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0},
		 					};
		
		// 1 - 가운데 pop
		// 2 - 상 중간, 3 - 상 말단, 4 - 하 중간, 5 - 하 말단
		// 6 - 좌 중간, 7 - 좌 말단, 8 - 우 중간, 8 - 우 말단
	
		int[][] WaterArray = { //물줄기
				{0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0}, 
				{0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0}, 
				{0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0},
				{0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0},
				{0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0}, 
				{0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0}, 
				{0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0},
				{0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0},
				{0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0}, 
				{0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0}, 
				{0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0},
				{0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0},
				{0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0}
		 					};
		
		
	// ----------------- 1P ---------------------------------------------------------------------------	
	
	int myx,myy;//플레이어 위치. 화면 좌표계에 *100 된 상태.
	int myspeed;//플레이어 이동 속도
	int mydegree;//플레이어 이동 방향
		//보통 4방향키-8방향 조작계에서는 이동 방향을 각도로 관리하지 않지만 여기서는 장래 터치스크린 인터페이스로
		//이식될 것을 고려해 4방향키 조작계를 0, 45, 90, 135, 180, 225, 270, 315도 방향으로 조작하는 것으로 한다.
	
	//int mywidth, myheight;//플레이어 캐릭터의 너비 높이
	
	//플레이어 캐릭터의 상태 
	//(0부터 순서대로 0 무적,1 등장(무적),2 온플레이,3 데미지(물풍선으로 바꾸면 될듯), 4 사망)
	//일단 바로 플레이 시작으로 넘어가도 될거같아서 init에서 2로 시작
	int mymode;
	int maxBubble = 5; //최대 버블 갯수 5개 (기본)
	int waterLength = 1; //줄기 길이
	
	//플레이어 이미지
	// 0 wait 1-상 2-하 3-좌 4-우
	int myimg;
	
	int trapCnt=0;
	int dCnt=0; 
	
	
	int mycnt; 
	boolean myshoot=false;//풍선 발사가 눌리고 있는가 
	//int myshield;//실드 남은 수비량 <<아님 봐서 이걸 바늘로..?일단 나중에
	
	boolean my_inv=false;//키보드 반전 ++아이템중에 먹으면 키보드 반전되는 그런거 있지 않았나? 일단 이것도 나중에
	
	boolean myDeath;
	
	
	
	//-----------------------------------------------------------------------------------------------
	
	
	// ----------------- 2P --------------------------------------------------------------------------
	
	int myx2,myy2;//플레이어 위치. 화면 좌표계에 *100 된 상태.
	int myspeed2;//플레이어 이동 속도
	int mydegree2;//플레이어 이동 방향
	
	//플레이어 캐릭터의 상태 
	//(0부터 순서대로 0 무적,1 등장(무적),2 온플레이,3 데미지(물풍선으로 바꾸면 될듯), 4 사망)
	//일단 바로 플레이 시작으로 넘어가도 될거같아서 init에서 2로 시작
	int mymode2;
	int maxBubble2 = 5; //최대 버블 갯수 5개 (기본)
	int waterLength2 = 1; //줄기 길이
	
	//플레이어 이미지
	// 0 wait 1-상 2-하 3-좌 4-우
	int myimg2;
	
	int trapCnt2=0;
	int dCnt2=0;
	
	int mycnt2; 
	boolean myshoot2=false;//풍선 발사가 눌리고 있는가 
	//int myshield2;//실드 남은 수비량 <<아님 봐서 이걸 바늘로..?일단 나중에
	
	boolean my_inv2=false;//키보드 반전 
	
	boolean myDeath2;
	
	//-----------------------------------------------------------
	
	
    int gScreenWidth = 1045; //게임 화면 너비
    int gScreenHeight = 785; //게임 화면 높이
    
	Vector bubble=new Vector();//물풍선 관리 - 몇 개 이상 
	Vector bubble2 = new Vector();
	
	Vector water=new Vector();
	
	
	//Vector enemies=new Vector();//적 캐릭터 관리.
	Vector effects=new Vector();//이펙트 관리
	//Vector items=new Vector();//아이템 관리 //얘도 봐서 배열로 빼야되나 
	
	

	

	//그래픽 관련 변수
	private Frame frame;
	private FileDialog fd;
	private JLabel lblMouseEvent;
	private Graphics gc;
	private int pen_size = 2; // minimum 2
	// 그려진 Image를 보관하는 용도, paint() 함수에서 이용한다.
	private Image panelImage = null; 
	private Graphics gc2 = null;
	


	private Image background=new ImageIcon("play_bg.png").getImage();//배경이미지
	
	ArcadeClientView clientView;
	boolean p1;
	
	
	
	public void focusGained(FocusEvent e) {
		//this.setFocusable(true);
	}
	public void focusLost(FocusEvent e) {
		this.requestFocus();
	}

	
	/**
	 * Create the frame.
	 * @throws BadLocationException 
	 */
	
	//생성자 --------------------------------------------------
	public ArcadeClientGameView(ArcadeClientView clientView, String roomId, boolean p1)  {
		
		
		this.clientView = clientView;
		this.p1=p1;
		
		ChatMsg msg = new ChatMsg(clientView.UserName, "999" , "게임시작 테스트"); //테스트
		clientView.SendObject(msg);
		
		//기본적인 윈도우 정보 세팅. 게임과 직접적인 상관은 없이 게임 실행을 위한 창을 준비하는 과정.
		showFrame(); //창 설정
		
		addFocusListener(this);
		addKeyListener(this);//키 입력 이벤트 리스너 활성화
		//addWindowListener(new MyWindowAdapter());//윈도우의 닫기 버튼 활성화
		
		this.gamescreen = new GameScreen(this); //화면 묘화를 위한 캔버스 객체
        this.gamescreen.setBounds(0, 0, 1029, 780);
        getContentPane().add(this.gamescreen);//Canvas 객체를 프레임에 올린다
        
        this.systeminit();
        this.initialize();
        
        //초기화
        Init_GAME(); 
		Init_MY(); 
		Init_MY2();//2p

		MapGenerator();//맵
		ItemGenerator();
		
		status = 2;
		mymode = 2;
		

	} // 생성자 끝 -----------------------------------------------------------
   
	public void systeminit() {//프로그램 초기화
        this.cnt = 0;
        this.delay = 17; // 17/1000초 = 58 (프레임/초)
        this.keybuff = 0; //1p 키 버퍼 초기화
        this.keybuff2 = 0;//2p 키 버퍼 초기화
        
        this.mainwork = new Thread(this);
        this.mainwork.start();
    }

    public void initialize() {//게임 초기화
        //this.Init_TITLE(); //타이틀 뜨게 하는 거
        this.gamescreen.repaint();
        
    }
    
 // 게임 돌아가는 스레드 ==============================================================
 	@Override
 	public void run() {
         while(true) {
             try {
                 if (this.roof) {
                	 
                	 
                	 //System.out.println(key);
                	 
                     this.pretime = System.currentTimeMillis();
                     this.gamescreen.repaint();//화면 리페인트
                     this.process();//각종 처리
                     this.keyprocess();//키 처리
                     this.keyprocess2();
                     
                     if (System.currentTimeMillis() - this.pretime < (long)this.delay) {
                    	//게임 루프를 처리하는데 걸린 시간을 체크해서 딜레이값에서 차감하여 딜레이를 일정하게 유지한다.
     					//루프 실행 시간이 딜레이 시간보다 크다면 게임 속도가 느려지게 된다.
                         Thread.sleep((long)this.delay - System.currentTimeMillis() + this.pretime);
                     }

                     if (this.status != 4) { //게임 오버상태가 아니라면 흘러가게함
                         ++this.cnt;
                         
                     }
                     continue;
                 }
             } catch (Exception var2) {
                 var2.printStackTrace();
             }

             return;
         }
 		
 	}
	
 	
	
	public void showFrame() { //프레임 그리기------------------------------------------
		setTitle("게임방"); //프레임 타이틀 지정
		setSize(1045,819);//프레임 크기
		setResizable(false); //창크기 변경불가
		setLocationRelativeTo(null);//창 가운데 뜨게
		setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		getContentPane().setLayout(null);
		setVisible(true);
	}
	
//	public void paint(Graphics g) {//배경 그리는 함수
//		g.drawImage(background, 0, 0, null);
//	}
	
	//-------------------------------------------------------------------

	
	public void keyPressedEvent(ChatMsg cm) {
		

		
		key = 0;
		key2 = 0;
		

		
		if(cm.UserName.equals(clientView.UserName)) { //내 움직임
			movePlayer1Pressed(Integer.parseInt(cm.data));
		}
		else { //상대 움직임
			movePlayer2Pressed(Integer.parseInt(cm.data));
		}
	
		System.out.println(key);
		
		
		 this.keybuff = key;
		 this.keybuff2 = key2;
		
	}
	
	
	public void keyReleasedEvent(ChatMsg cm) {
		
		
		keyR = 0;
		keyR2 =0;
		
		if(cm.UserName.equals(clientView.UserName)) { //내 움직임
			movePlayer1Released(Integer.parseInt(cm.data));
		}
		else { //상대 움직임
			movePlayer2Released(Integer.parseInt(cm.data));
		}
		


		this.keybuff = keyR;
		this.keybuff2 = keyR2;
	}
	
	public void movePlayer1Pressed(int keyCode) {
		
		
		if(status==2){ //게임이 playing상태 일때
			switch(keyCode){
			// 1P
			case KeyEvent.VK_SPACE:
				key|=BUBBLE_PRESSED;
				break;
			case KeyEvent.VK_LEFT:
				key|=LEFT_PRESSED; //멀티키의 누르기 처리
				break;
			case KeyEvent.VK_UP:
				key|=UP_PRESSED;
				break;
			case KeyEvent.VK_RIGHT:
				key|=RIGHT_PRESSED;
				break;
			case KeyEvent.VK_DOWN:
				key|=DOWN_PRESSED;
				break;
			default:
				break;
			
			}
			

			
		} else if(status!=2) {
//			keybuff=keyCode.getKeyCode();
//			keybuff2=e.getKeyCode();
		}
	}
	
	public void movePlayer2Pressed(int keyCode) {
		
		
		
		switch(keyCode){
		case KeyEvent.VK_SPACE:
			key2|=BUBBLE_PRESSED2;
			break;
		case KeyEvent.VK_LEFT:
			key2|=LEFT_PRESSED2;//멀티키의 누르기 처리
			break;
		case KeyEvent.VK_UP:
			key2|=UP_PRESSED2;
			break;
		case KeyEvent.VK_RIGHT:
			key2|=RIGHT_PRESSED2;
			break;
		case KeyEvent.VK_DOWN:
			key2|=DOWN_PRESSED2;
			break;
		default:
			break;
		}
		
	}
	
	public void movePlayer1Released(int keyCode) {
		
		System.out.println("starttttttttttttttttt");
		System.out.println(this.keybuff);
	
		
		keyR = this.keybuff;
		
		System.out.println(keyR);
		
		switch(keyCode) {
			case KeyEvent.VK_SPACE:
				keyR&=~BUBBLE_PRESSED;
				break;
			case KeyEvent.VK_LEFT:
				keyR&=~LEFT_PRESSED;//멀티키의 떼기 처리
				break;
			case KeyEvent.VK_UP:
				keyR&=~UP_PRESSED;
				break;
			case KeyEvent.VK_RIGHT:
				keyR&=~RIGHT_PRESSED;
				break;
			case KeyEvent.VK_DOWN:
				keyR&=~DOWN_PRESSED;
				break;
			default:
				break;
		}
		
		
	}
	
	public void movePlayer2Released(int keyCode) {
		
		keyR2 = this.keybuff2;
		
		switch(keyCode) {
			// 2P
		case KeyEvent.VK_SPACE:
			keyR2&=~BUBBLE_PRESSED2; 
			break;
		case KeyEvent.VK_LEFT:
			keyR2&=~LEFT_PRESSED2;//멀티키의 떼기 처리
			break;
		case KeyEvent.VK_UP:
			keyR2&=~UP_PRESSED2;
			break;
		case KeyEvent.VK_RIGHT:
			keyR2&=~RIGHT_PRESSED2;
			break;
		case KeyEvent.VK_DOWN:
			keyR2&=~DOWN_PRESSED2;
			break;
		default:
			break;
		}
	}
	
	
	// 키 이벤트 리스너 처리
	@Override
	public void keyPressed(KeyEvent e) { //키보드를 눌렀을 때 호출, 모든 키보드에 반응
		
		ChatMsg msg = new ChatMsg(clientView.UserName, "900" , Integer.toString(e.getKeyCode())); 
		clientView.SendObject(msg);
		
	}


	@Override
	public void keyReleased(KeyEvent e) { //키보드를 떼었을 때, 모든 키보드에 반응
		
		ChatMsg msg = new ChatMsg(clientView.UserName, "1000", Integer.toString(e.getKeyCode())); //테스트
		clientView.SendObject(msg);
		

	}
	@Override
		public void keyTyped(KeyEvent e) { //문자를 눌렀을 때 호출, 문자키에만 반응
			
		   
		}
	
	//==============================================================================
	
	// 각종 판단, 변수나 이벤트, CPU 관련 처리
	private void process(){
		switch(status){
		case 0://타이틀화면 - 사용X
			break;
		case 1://스타트
			process_MY();
			if(mymode==2) status=2;
			
			process_MY2();
			if(mymode2==2) status=2;
			break;
		case 2://playing
			process_MY();
			process_MY2();
			process_BUBBLE();
			process_WATER();
			
			break;
		case 3://게임오버
//			process_ENEMY();
//			process_BULLET();
//			process_GAMEFLOW();
			break;
		case 4:
			break;
		default:
			break;
		}
		if(status!=4) gamecnt++;
		}

    
    //==============================================================================
    
    // 키 입력 처리
 	// 키 이벤트에서 입력 처리를 할 경우, 
	//이벤트 병목현상이 발생할 수 있으므로 이벤트에서는 키 버퍼만을 변경하고,
    //루프 내에서 버퍼값에 따른 처리를 한다.
 	
	
	//----------------1P------------------
    private void keyprocess() {
    	

    	
    	switch (this.status) {
    		case 0:
    			break;
    		case 2: //game playing	
    			if(mymode==2)
    				switch(keybuff) {
    				
    				//mydegree:플레이어 방향
    				
    				//my img :플레이어 이미지
    				// 0 wait 1-상 2-하 3-좌 4-우
    				
    				case 0:
    					mydegree=-1;
    					myimg=0;
    					break;
    				case BUBBLE_PRESSED:
    					if(gamecnt%bubbleCnt==0) {  //gamecnt%bubbleCnt==0
    						//Bubble bubbles = new Bubble(myx, myy, 1);
    						make_BUBBLE(myx,myy, waterLength, 1);
        					//bubble.add(bubbles);
    					}
    					
    					break;
    				case UP_PRESSED:
    					mydegree=0;
    					myimg=1;
    					break;
    				case DOWN_PRESSED:
    					mydegree=180;
    					myimg=2;
    					break;
    				case LEFT_PRESSED:
    					mydegree=90;
    					myimg=3;
    					break;
    				case RIGHT_PRESSED:
    					mydegree=270;
    					myimg=4;
    					break;

    					
    				case UP_PRESSED|BUBBLE_PRESSED:
    					if(gamecnt%bubbleCnt==0) { //gamecnt%bubbleCnt==0
    						make_BUBBLE(myx,myy, waterLength, 1);
    					}
    					mydegree=0;
    					myimg=1;
    					break;
    				
    				case LEFT_PRESSED|BUBBLE_PRESSED:
    					if(gamecnt%bubbleCnt==0) { //gamecnt%bubbleCnt==0
    						make_BUBBLE(myx,myy, waterLength, 1);
    					}
    					mydegree=90;
    					myimg=3;
    					break;
    				
    				case RIGHT_PRESSED|BUBBLE_PRESSED:
    					if(gamecnt%bubbleCnt==0) { //gamecnt%bubbleCnt==0
    						make_BUBBLE(myx,myy, waterLength, 1);
    					}
    					mydegree=270;
    					myimg=4;
    					break;
    				case UP_PRESSED|LEFT_PRESSED:
    					mydegree=0;
    					myimg=1;
    					break;
    				case UP_PRESSED|LEFT_PRESSED|BUBBLE_PRESSED:
    					if(gamecnt%bubbleCnt==0) { 
    						make_BUBBLE(myx,myy, waterLength, 1);
    					}
    					mydegree=0;
    					myimg=1;
    					break;
    				case UP_PRESSED|RIGHT_PRESSED:
    					mydegree=0;
    					myimg=1;
    					break;
    				case UP_PRESSED|RIGHT_PRESSED|BUBBLE_PRESSED:
    					if(gamecnt%bubbleCnt==0) { 
    						make_BUBBLE(myx,myy, waterLength, 1);
    					}
    					mydegree=0;
    					myimg=1;
    					break;
    				
    				case DOWN_PRESSED|BUBBLE_PRESSED:
    					if(gamecnt%bubbleCnt==0) {
    						make_BUBBLE(myx,myy, waterLength, 1);
    					}
    					mydegree=180;
    					myimg=2;
    					break;
    				case DOWN_PRESSED|LEFT_PRESSED:
    					mydegree=180;
    					myimg=2;
    					break;
    				case DOWN_PRESSED|LEFT_PRESSED|BUBBLE_PRESSED:
    					if(gamecnt%bubbleCnt==0) { 
    						make_BUBBLE(myx,myy, waterLength, 1);
    					}
    					mydegree=180;
    					myimg=2;
    					break;
    				case DOWN_PRESSED|RIGHT_PRESSED:
    					mydegree=180;
    					myimg=2;
    					break;
    				case DOWN_PRESSED|RIGHT_PRESSED|BUBBLE_PRESSED:
    					if(gamecnt%bubbleCnt==0) {
    						make_BUBBLE(myx,myy, waterLength, 1);
    					}
    					mydegree=180;
    					myimg=2;
    					break;
    				default:
    					//System.out.println(""+keybuff);
    					keybuff=0;
    					mydegree=-1;
    					//myimg=0;
    					break;
    				}
    			else if(mymode==3) { //물풍선에 잡혀있는 상태
    				switch(keybuff) {
    			
    				case 0:
    					mydegree=-1;
    					break;
    				case UP_PRESSED:
    					mydegree=0;
    					break;
    				case DOWN_PRESSED:
    					mydegree=180;
    					break;
    				case LEFT_PRESSED:
    					mydegree=90;
    					break;
    				case RIGHT_PRESSED:
    					mydegree=270;
    					break;

    					
    				case UP_PRESSED|BUBBLE_PRESSED:
    					mydegree=0;
    					break;
    				
    				case LEFT_PRESSED|BUBBLE_PRESSED:
    					mydegree=90;
    					break;
    				
    				case RIGHT_PRESSED|BUBBLE_PRESSED:
    					mydegree=270;
    					break;
    				case UP_PRESSED|LEFT_PRESSED:
    					mydegree=0;
    					break;
    					
    					//사실 스페이스바 입력은 빼버려도 되는데
    					//갇혀도 스페이스바 괜히 누르는 경우 있으니 그냥 냅둠
    				case UP_PRESSED|LEFT_PRESSED|BUBBLE_PRESSED:
    					mydegree=0;
    					break;
    				case UP_PRESSED|RIGHT_PRESSED:
    					mydegree=0;
    					break;
    				case UP_PRESSED|RIGHT_PRESSED|BUBBLE_PRESSED:
    					mydegree=0;
    					break;
    				case DOWN_PRESSED|BUBBLE_PRESSED:
    					mydegree=180;
    					break;
    				case DOWN_PRESSED|LEFT_PRESSED:
    					mydegree=180;
    					break;
    				case DOWN_PRESSED|LEFT_PRESSED|BUBBLE_PRESSED:
    					mydegree=180;
    					break;
    				case DOWN_PRESSED|RIGHT_PRESSED:
    					mydegree=180;
    					break;
    				case DOWN_PRESSED|RIGHT_PRESSED|BUBBLE_PRESSED:
    					mydegree=180;
    					break;
    				default:
    					//System.out.println(""+keybuff);
    					keybuff=0;
    					mydegree=-1;
    					break;
    				}
    			}
//    			else if(mymode==4) { //사망모션
//    				
//    			}
    		case 3:
    		{
    			//게임 오버
    		}
    	
    	}
    }
    
    
    
    
    //----------------2P------------------============================================================
    private void keyprocess2() {
    	
  
    	
    	switch (this.status) {
    		case 0:
    			break;
    		case 2: //game playing	
    			if(mymode2==2)
    				switch(keybuff2) {
    				
    				//mydegree:플레이어 방향
    				
    				//my img :플레이어 이미지
    				
    				 
    				
    				// 0 wait 1-상 2-하 3-좌 4-우
    				
    				case 0:
    					mydegree2=-1;
    					myimg2=0;
    					break;
    				case BUBBLE_PRESSED2:
    					if(gamecnt%bubbleCnt==0) {  //gamecnt%bubbleCnt==0
    						//Bubble bubbles = new Bubble(myx, myy, 1);
    						make_BUBBLE(myx2,myy2, waterLength, 2);
        					//bubble.add(bubbles);
    					}
    					
    					break;
    				case UP_PRESSED2:
    					mydegree2=0;
    					myimg2=1;
    					break;
    				case DOWN_PRESSED2:
    					mydegree2=180;
    					myimg2=2;
    					break;
    				case LEFT_PRESSED2:
    					mydegree2=90;
    					myimg2=3;
    					break;
    				case RIGHT_PRESSED2:
    					mydegree2=270;
    					myimg2=4;
    					break;

    					
    				case UP_PRESSED|BUBBLE_PRESSED:
    					if(gamecnt%bubbleCnt==0) { //gamecnt%bubbleCnt==0
    						make_BUBBLE(myx2,myy2, waterLength2, 2);
    					}
    					mydegree2=0;
    					myimg2=1;
    					break;
    				
    				case LEFT_PRESSED|BUBBLE_PRESSED:
    					if(gamecnt%bubbleCnt==0) { //gamecnt%bubbleCnt==0
    						make_BUBBLE(myx2,myy2, waterLength2, 2);
    					}
    					mydegree2=90;
    					myimg2=3;
    					break;
    				
    				case RIGHT_PRESSED|BUBBLE_PRESSED:
    					if(gamecnt%bubbleCnt==0) { //gamecnt%bubbleCnt==0
    						make_BUBBLE(myx2,myy2, waterLength2, 2);
    					}
    					mydegree2=270;
    					myimg2=4;
    					break;
    				case UP_PRESSED|LEFT_PRESSED:
    					mydegree2=0;
    					myimg2=1;
    					break;
    				case UP_PRESSED|LEFT_PRESSED|BUBBLE_PRESSED:
    					if(gamecnt%bubbleCnt==0) { 
    						make_BUBBLE(myx2,myy2, waterLength2, 2);
    					}
    					mydegree2=0;
    					myimg2=1;
    					break;
    				case UP_PRESSED|RIGHT_PRESSED:
    					mydegree2=0;
    					myimg2=1;
    					break;
    				case UP_PRESSED|RIGHT_PRESSED|BUBBLE_PRESSED:
    					if(gamecnt%bubbleCnt==0) { 
    						make_BUBBLE(myx2,myy2, waterLength2, 2);
    					}
    					mydegree2=0;
    					myimg2=1;
    					break;
    				
    				case DOWN_PRESSED|BUBBLE_PRESSED:
    					if(gamecnt%bubbleCnt==0) {
    						make_BUBBLE(myx2,myy2, waterLength2, 2);
    					}
    					mydegree2=180;
    					myimg2=2;
    					break;
    				case DOWN_PRESSED|LEFT_PRESSED:
    					mydegree2=180;
    					myimg2=2;
    					break;
    				case DOWN_PRESSED|LEFT_PRESSED|BUBBLE_PRESSED:
    					if(gamecnt%bubbleCnt==0) { 
    						make_BUBBLE(myx2,myy2, waterLength2, 2);
    					}
    					mydegree2=180;
    					myimg2=2;
    					break;
    				case DOWN_PRESSED|RIGHT_PRESSED:
    					mydegree2=180;
    					myimg2=2;
    					break;
    				case DOWN_PRESSED|RIGHT_PRESSED|BUBBLE_PRESSED:
    					if(gamecnt%bubbleCnt==0) {
    						make_BUBBLE(myx2,myy2, waterLength2, 2);
    					}
    					mydegree2=180;
    					myimg2=2;
    					break;
    				default:
    					//System.out.println(""+keybuff2);
    					keybuff2=0;
    					mydegree2=-1;
    					//myimg=0;
    					break;
    				}
    			else if(mymode2==3) { //물풍선에 잡혀있는 상태
    				switch(keybuff2) {
    			
    				case 0:
    					mydegree2=-1;
    					break;
    				case UP_PRESSED:
    					mydegree2=0;
    					break;
    				case DOWN_PRESSED:
    					mydegree2=180;
    					break;
    				case LEFT_PRESSED:
    					mydegree2=90;
    					break;
    				case RIGHT_PRESSED:
    					mydegree2=270;
    					break;

    					
    				case UP_PRESSED|BUBBLE_PRESSED:
    					mydegree2=0;
    					break;
    				
    				case LEFT_PRESSED|BUBBLE_PRESSED:
    					mydegree2=90;
    					break;
    				
    				case RIGHT_PRESSED|BUBBLE_PRESSED:
    					mydegree2=270;
    					break;
    				case UP_PRESSED|LEFT_PRESSED:
    					mydegree2=0;
    					break;
    					
    					//사실 스페이스바 입력은 빼버려도 되는데
    					//갇혀도 스페이스바 괜히 누르는 경우 있으니 그냥 냅둠
    				case UP_PRESSED|LEFT_PRESSED|BUBBLE_PRESSED:
    					mydegree2=0;
    					break;
    				case UP_PRESSED|RIGHT_PRESSED:
    					mydegree2=0;
    					break;
    				case UP_PRESSED|RIGHT_PRESSED|BUBBLE_PRESSED:
    					mydegree2=0;
    					break;
    				case DOWN_PRESSED|BUBBLE_PRESSED:
    					mydegree2=180;
    					break;
    				case DOWN_PRESSED|LEFT_PRESSED:
    					mydegree2=180;
    					break;
    				case DOWN_PRESSED|LEFT_PRESSED|BUBBLE_PRESSED:
    					mydegree2=180;
    					break;
    				case DOWN_PRESSED|RIGHT_PRESSED:
    					mydegree2=180;
    					break;
    				case DOWN_PRESSED|RIGHT_PRESSED|BUBBLE_PRESSED:
    					mydegree2=180;
    					break;
    				default:
    					//System.out.println(""+keybuff2);
    					keybuff2=0;
    					mydegree2=-1;
    					break;
    				}
    			}
    	
    	}
    }
    
    
  //==============================================================================
    //각종 설정 초기화
	public void Init_GAME(){
		int i;
		/*gamescreen.title=null;
		gamescreen.title_key=null;
		System.gc();*/

		//각종 이미지 설정
		gamescreen.bg=makeImage("./play_bg.png");//bg.png

		//일단 블록 여러 종류로
		//for(i=0;i<8;i++) gamescreen.block[i]=makeImage("./title/block" + i + ".png"); 
		//gamescreen.block[0]=makeImage("./tile/block3.png"); 
		for(i=1;i<12;i++) {
	         gamescreen.block[i]=makeImage("./tile/block"+i+".png");
		 }
		
		//아이템--0
		//스피드업
		for(i=1;i<4;i++) {
			gamescreen.speed[i]=makeImage("./item/speed"+i+".png");
		}
		//물줄기 업
		for(i=1;i<4;i++) {
			gamescreen.Bpower[i]=makeImage("./item/Bpower"+i+".png");
		}
		//풍선개수 업
		for(i=1;i<4;i++) {
			gamescreen.Bmax[i]=makeImage("./item/Bmax"+i+".png");
		}
		
		
		//물풍선
		for(i=0;i<4;i++) 
			gamescreen.bubble[i]=makeImage("./waterballoon/bomb"+i+".png"); 
		
		
		//물줄기
		for(i=0;i<6;i++)
			gamescreen.waterPop[i]=makeImage("./waterballoon/pop"+i+".png");
		
		for(i=0;i<11;i++){
			if(i<10) {
				
				if(i!=3&&i!=4) {
					gamescreen.waterUpM[i]=makeImage("./waterballoon/up2_"+i+".png");
					gamescreen.waterUpE[i]=makeImage("./waterballoon/up1_"+i+".png");
					gamescreen.waterDownM[i]=makeImage("./waterballoon/down2_"+i+".png");
					gamescreen.waterDownE[i]=makeImage("./waterballoon/down1_"+i+".png");
					gamescreen.waterLeftM[i]=makeImage("./waterballoon/left2_"+i+".png");
					gamescreen.waterLeftE[i]=makeImage("./waterballoon/left1_"+i+".png");
					gamescreen.waterRightM[i]=makeImage("./waterballoon/right2_"+i+".png");
					gamescreen.waterRightE[i]=makeImage("./waterballoon/right1_"+i+".png");
				}
				else {
					gamescreen.waterUpM[i]=makeImage("./waterballoon/up2_"+2+".png");
					gamescreen.waterUpE[i]=makeImage("./waterballoon/up1_"+2+".png");
					gamescreen.waterDownM[i]=makeImage("./waterballoon/down2_"+2+".png");
					gamescreen.waterDownE[i]=makeImage("./waterballoon/down1_"+2+".png");
					gamescreen.waterLeftM[i]=makeImage("./waterballoon/left2_"+2+".png");
					gamescreen.waterLeftE[i]=makeImage("./waterballoon/left1_"+2+".png");
					gamescreen.waterRightM[i]=makeImage("./waterballoon/right2_"+2+".png");
					gamescreen.waterRightE[i]=makeImage("./waterballoon/right1_"+2+".png");
				}
				
			}
				
			else {
				gamescreen.waterUpM[i]=makeImage("./waterballoon/up2_"+i+".png");
				gamescreen.waterUpE[i]=makeImage("./waterballoon/up1_"+i+".png");
				gamescreen.waterDownM[i]=makeImage("./waterballoon/down2_"+i+".png");
				gamescreen.waterDownE[i]=makeImage("./waterballoon/down1_"+i+".png");
				gamescreen.waterLeftM[i]=makeImage("./waterballoon/left2_"+i+".png");
				gamescreen.waterLeftE[i]=makeImage("./waterballoon/left1_"+i+".png");
				gamescreen.waterRightM[i]=makeImage("./waterballoon/right2_"+i+".png");
				gamescreen.waterRightE[i]=makeImage("./waterballoon/right1_"+i+".png");
			}
				
		}
		
			
		
		
		
		keybuff=0;
		keybuff2=0;
//		bullets.clear();
//		enemies.clear();
//		effects.clear();
//		items.clear();
//		level=0;
		gamecnt=0;
	}
		
	public void Init_MY(){
		
		gamescreen.chr=makeImage("./player/wait0.png");
		
		for(int i=0;i<8;i++)
			gamescreen.chrUp[i]=makeImage("./player/up"+i+".png");
		
		for(int i=0;i<8;i++)
			gamescreen.chrDown[i]=makeImage("./player/down"+i+".png");
		
		for(int i=0;i<6;i++)
			gamescreen.chrLeft[i]=makeImage("./player/left"+i+".png");
		
		for(int i=0;i<6;i++)
			gamescreen.chrRight[i]=makeImage("./player/right"+i+".png");
		
		for(int i=0;i<13;i++){
			if(i<10)
				gamescreen.chrTrap[i]=makeImage("./player/trap"+i+".png"); //trap 0~9
			else
				gamescreen.chrTrap[i]=makeImage("./player/trap"+i+".png"); //trap10 11 12 
		}
		
		for(int i=0;i<10;i++) //사망모션
			gamescreen.chrDeath[i]=makeImage("./player/death"+i+".png");
		
		Init_MYDATA();
	}
	public void Init_MYDATA(){
		
		//플레이어의 x,y는 *100된 상태
		//리스폰 장소
		
		if(p1) { //본인이 player1 이라면 자기 위치를 왼쪽으로
			myx=5000;
			myy=17000;
		}
		else { 	//player2라면 오른쪽으로
			myx=68000;
			myy=10000;
		}
		 
		
		myspeed=4; //속도
		mydegree=-1; //방향
		//mywidth, myheight;//플레이어 캐릭터의 너비 높이
		mymode=1; //게임시작
		myimg=2;
		mycnt=0;
		keybuff=0;
	}
	
	//---------------2P---------------
	public void Init_MY2(){
		
		gamescreen.chr=makeImage("./player/wait0.png");
		
		for(int i=0;i<8;i++)
			gamescreen.chrUp[i]=makeImage("./player/up"+i+".png");
		
		for(int i=0;i<8;i++)
			gamescreen.chrDown[i]=makeImage("./player/down"+i+".png");
		
		for(int i=0;i<6;i++)
			gamescreen.chrLeft[i]=makeImage("./player/left"+i+".png");
		
		for(int i=0;i<6;i++)
			gamescreen.chrRight[i]=makeImage("./player/right"+i+".png");
		
		for(int i=0;i<13;i++){
			if(i<10)
				gamescreen.chrTrap[i]=makeImage("./player/trap"+i+".png"); //trap 0~9
			else
				gamescreen.chrTrap[i]=makeImage("./player/trap"+i+".png"); //trap10 11 12 
		}
		for(int i=0;i<10;i++) //사망모션
			gamescreen.chrDeath[i]=makeImage("./player/death"+i+".png");
		Init_MYDATA2();
	}
	public void Init_MYDATA2(){
		
		//플레이어의 x,y는 *100된 상태
		//리스폰 장소
		
		if(p1) { //자기가 player1이라면 상대측 나오는 장소를 오른쪽으로
			myx2=68000;
			myy2=10000;
		}
		else{	//player2라면 상대측 나오는 장소를 왼쪽으로
			myx2=5000;
			myy2=17000;
		}
		 
		
		myspeed2=4; //속도
		mydegree2=-1; //방향
		//mywidth, myheight;//플레이어 캐릭터의 너비 높이
		mymode2=1; //게임시작
		myimg2=2;
		mycnt2=0;
		keybuff2=0;
	}
	
	//process-------------------------------------------------------------------
	
	public void process_MY(){
		//Bubble bubble;
		
		//플레이어 캐릭터의 상태 
		//0 무적,1 등장(무적),2 온플레이,3 데미지(물풍선으로 바꾸면 될듯), 4 사망
		//사실 무적모드를 해놔야되나 싶기도 해서 바로 mymode 2에서 시작
		switch(mymode){
		case 1:
			//원래는 등장해서 앞쪽으로 쭉 나오는거 구현된 부분
//			myx+=200;
//			if(myx>20000) mymode=2;
//			break;
			
			//여기서 플레이어 등장 위치를 조절할 수도 있을거같아서
			//mymode 1로 시작하고, mymode를 2로 변경하는 방식으로 진행
			mymode=2; //playing
			
		case 0:
			//무적 상태.... 안쓸듯
			if(mycnt--==0) {
				mymode=2;
				myimg=0;
			}
		case 2: // Playing!!
			
			//mydegree : 캐릭터 이동 방향

			if(mydegree!=-1&&my_inv) //키보드반전처리
				mydegree=(mydegree+180)%360;
			
			if(mydegree>-1) { //키보드 방향대로 속도 맞춰서 이동
				myx-=(myspeed*Math.sin(Math.toRadians(mydegree))*100);
				myy-=(myspeed*Math.cos(Math.toRadians(mydegree))*100);
			}

			break;
		case 3: // trap상태
			if(mydegree>-1) { //키보드 방향대로 속도 맞춰서 이동
				myx-=(Math.sin(Math.toRadians(mydegree))*50);
				myy-=(Math.cos(Math.toRadians(mydegree))*50);
			}
			
			
			if(trapCnt>=300) {
				System.out.println("사망모션 진입--------");
				mymode=4;
			}
			
			trapCnt++;
			
			break;
			
			
		case 4: //사망모션
			if(dCnt>=500) {
				System.out.println("사망 -------");
				myDeath=true;
				status=3; // 게임 종료
				
			}
			
			dCnt++;
			
			break;
		}
		//여기가 사이즈 제한
		if(myx<4800) myx=4800;
		if(myx>78000) myx=78000;
		if(myy<5900) myy=5900;  //이거보다 작게 잡으면 물방울이 위로 올라가버림
		if(myy>69000) myy=69000;
		
		
		
		//플레이어가 블록에 충돌되면 처리
	    for(int i=0;i<13;i++) {
	    	for(int j=0;j<15;j++) {
	    		if(MapArray[i][j]!=0) {
	    			
	    			if(MapArray[i][j]==-1) //얼마전에 생성된 물풍선은 움직일 수 있게
	    				break;
	    			
	    			if((myx>=bkx*(j)*100)&&(myy>=(bky+1)*(i)*100)&&
	    				(myx<=bkx*(j+2)*100)&&(myy<=(bky)*(i+2)*100)) {//물체에 닿는지 검사	    				
	    				//System.out.printf("B [ %d, %d ]\n", ((myx)-(bkx*100*j)),((myy)-(bky*100*i)));
	    				if((300<(myx)-(bkx*100*j) && 4300>(myx)-(bkx*100*j))&&
	    					(1400<(myy)-(bky*100*i) && 10000>(myy)-(bky*100*i))){//닿은곳이 왼쪽일때 처리
//	    					System.out.println("왼쪽이 닿았다.");
	    					myx = myx - 400;
	    					if(myspeed>4) {//스피드 증가에 따른 맵뚫 방지
	    						myx = myx - 300;
	    					}
	    				}
	    				else if((9000<(myx)-(bkx*100*j) && 10200>(myx)-(bkx*100*j))&&
		    					(1200<(myy)-(bky*100*i) && 10000>(myy)-(bky*100*i))){//닿은곳이 오른쪽일때 처리
//	    					System.out.println("오른쪽이 닿았다.");
	    					myx = myx + 400;
	    					if(myspeed>4) {
	    						myx = myx + 300;
	    					}
	    				}
	    				else if((200<(myx)-(bkx*100*j) && 10200>(myx)-(bkx*100*j))&&
	    						(1400<(myy)-(bky*100*i) && 3800>=(myy)-(bky*100*i))){//닿은곳이 위쪽일때 처리
//	    					System.out.println("위쪽이 닿았다.");
	    					myy = myy - 400;
	    					if(myspeed>4) {
	    						myy = myy - 300;
	    					}
	    				}
	    				else if((300<(myx)-(bkx*100*j) && 10200>(myx)-(bkx*100*j))&&
	    						(8000<(myy)-(bky*100*i) && 10000>(myy)-(bky*100*i))){//닿은곳이 아래쪽일때 처리
//	    					System.out.println("아래쪽이 닿았다.");
	    					myy = myy + 400;
	    					if(myspeed>4) {
	    						myy = myy + 300;
	    					}
	    				}
	                }
	            }
	         }
	    }
	    
	    
	    
	    //플레이어가 아이템에 충돌되면 처리
	    for(int i=0;i<13;i++) {
	    	for(int j=0;j<15;j++) {
	    		if(ItemArray[i][j]!=0) {
	    			if((myx>=bkx*(j)*100)&&(myy>=(bky)*(i)*100)&&
		    				(myx<=bkx*(j+2)*100)&&(myy<=(bky)*(i+2)*100)) {//아이템에 닿는지 검사
	    				//System.out.printf("I [ %d, %d ]\n", ((myx)-(bkx*100*j)),((myy)-(bky*100*i)));
	    				
	    				if(ItemArray[i][j]==1) {//충돌한 아이템이 1(스피드) 이면,
	    					if((3000<(myx)-(bkx*100*j) && 7000>(myx)-(bkx*100*j))&& //아이템은 깊게 들어가야 획득됨
	    						((3000<(myy)-(bky*100*i))&&(7000>(myy)-(bky*100*i)))){
	    						ItemArray[i][j]=0;// 아이템 삭제
	    						//System.out.println("1");
	    						new effectSound("./music/eatItem.wav");//아이템 획득 사운드
		    					myspeed++; //속도가 1씩 증가하게 된다.
	    					}
	    					if(myspeed > 7) {
	    						myspeed = 7; //속도의 최고속도는 7이다.
	    					}
	    					//System.out.printf("현재속도 : %d \n",myspeed);
	    				}
	    				
	    				else if(ItemArray[i][j]==2) {//충돌한 아이템이 2(물줄기) 이면,
	    					if((3000<(myx)-(bkx*100*j) && 7000>(myx)-(bkx*100*j))&& //아이템은 깊게 들어가야 획득됨
		    						((3000<(myy)-(bky*100*i))&&(7000>(myy)-(bky*100*i)))){
	    						//System.out.println("2");
	    						ItemArray[i][j]=0;// 아이템 삭제
	    						if(!(waterLength>=5))
		    						waterLength++;
	    						new effectSound("./music/eatItem.wav");//아이템 획득 사운드
	    					}
	    				}
	    				
	    				else if(ItemArray[i][j]==3) {//충돌한 아이템이 3(풍선개수 증가) 이면,
	    					if((3000<(myx)-(bkx*100*j) && 7000>(myx)-(bkx*100*j))&& //아이템은 깊게 들어가야 획득됨
		    						((3000<(myy)-(bky*100*i))&&(7000>(myy)-(bky*100*i)))){
	    						//System.out.println("3");
	    						ItemArray[i][j]=0;// 아이템 삭제
	    						new effectSound("./music/eatItem.wav");//아이템 획득 사운드
	    						maxBubble ++; //물풍선의 최대 개수가 1씩 증가한다.
	    					}
	    					if(maxBubble > 10) {
    							maxBubble = 10; //물풍선 최대 개수는 10이다.
    						}
    						//System.out.printf("최대풍선개수 : %d \n",maxBubble);
	    				}
	                }
	            }
	         }
	    }
	    
	    //플레이어 물줄기 검사
	    for(int i=0;i<13;i++) {
	    	for(int j=0;j<15;j++) {
	    		if(WaterArray[i][j]!=0) { 
	    			if((myx>=bkx*(j)*100)&&(myy>=(bky)*(i)*100)&&
		    				(myx<=bkx*(j+2)*100)&&(myy<=(bky)*(i+2)*100)) {//물줄기에 닿는지 검사
	    				//물줄기는 아이템보다는 덜 깊게 들어가도 판정됨
	    				if((2500<(myx)-(bkx*100*j) && 8000>(myx)-(bkx*100*j))&& 
	    						((2500<(myy)-(bky*100*i))&&(8000>(myy)-(bky*100*i)))){
	    				//System.out.println("trap-----------------------------------------------------");
	    				mymode=3; //물풍선에 갇힘
	    				}
	                }
	            }
	         }
	    }
	    
	}//---------------process_MY 끝---------------------------------------------------------------------
	
	//---------------2P--------------------------------------------------------------------------------
	public void process_MY2(){
		//Bubble bubble;
		
		//플레이어 캐릭터의 상태 
		//0 무적,1 등장(무적),2 온플레이,3 데미지(물풍선으로 바꾸면 될듯), 4 사망
		//사실 무적모드를 해놔야되나 싶기도 해서 바로 mymode 2에서 시작
		switch(mymode2){
		case 1:
			mymode2=2; //playing
		case 0:
			//무적 상태.... 안쓸듯
			if(mycnt2--==0) {
				mymode2=2;
				myimg2=0;
			}
		case 2: // Playing!!
			
			//mydegree : 캐릭터 이동 방향

			if(mydegree2!=-1&&my_inv2) //키보드반전처리
				mydegree2=(mydegree2+180)%360;
			
			if(mydegree2>-1) { //키보드 방향대로 속도 맞춰서 이동
				myx2-=(myspeed2*Math.sin(Math.toRadians(mydegree2))*100);
				myy2-=(myspeed2*Math.cos(Math.toRadians(mydegree2))*100);
			}

			break;
		case 3: // trap상태
			if(mydegree2>-1) { //키보드 방향대로 속도 맞춰서 이동
				myx2-=(Math.sin(Math.toRadians(mydegree2))*50);
				myy2-=(Math.cos(Math.toRadians(mydegree2))*50);
			}
			
			
			if(trapCnt2>=300) {
				System.out.println("캐릭터 사망--------");
				mymode2=4;
			}
			
			trapCnt2++;
			
			break;
			
			
		case 4: //죽은 경우
			if(dCnt2>=500) {
				System.out.println("사망 모션----");
				myDeath2=true;
				status=3;
			}
			
			dCnt2++;
			
			break;
		}
		//여기가 사이즈 제한
		if(myx2<4800) myx2=4800;
		if(myx2>78000) myx2=78000;
		if(myy2<5900) myy2=5900;  //이거보다 작게 잡으면 물방울이 위로 올라가버림
		if(myy2>69000) myy2=69000;
		
		
		
		//플레이어가 블록에 충돌되면 처리
	    for(int i=0;i<13;i++) {
	    	for(int j=0;j<15;j++) {
	    		if(MapArray[i][j]!=0) {
	    			
	    			if(MapArray[i][j]==-1) //얼마전에 생성된 물풍선은 움직일 수 있게
	    				break;
	    			
	    			if((myx2>=bkx*(j)*100)&&(myy2>=(bky+1)*(i)*100)&&
	    				(myx2<=bkx*(j+2)*100)&&(myy2<=(bky)*(i+2)*100)) {//물체에 닿는지 검사	    				
	    				//System.out.printf("B2 [ %d, %d ]\n", ((myx2)-(bkx*100*j)),((myy2)-(bky*100*i)));
	    				if((300<(myx2)-(bkx*100*j) && 4300>(myx2)-(bkx*100*j))&&
	    					(1400<(myy2)-(bky*100*i) && 10000>(myy2)-(bky*100*i))){//닿은곳이 왼쪽일때 처리
//	    					System.out.println("왼쪽이 닿았다.");
	    					myx2 = myx2 - 400;
	    					if(myspeed2>4) {//스피드 증가에 따른 맵뚫 방지
	    						myx2 = myx2 - 300;
	    					}
	    				}
	    				else if((9000<(myx2)-(bkx*100*j) && 10200>(myx2)-(bkx*100*j))&&
		    					(1200<(myy2)-(bky*100*i) && 10000>(myy2)-(bky*100*i))){//닿은곳이 오른쪽일때 처리
//	    					System.out.println("오른쪽이 닿았다.");
	    					myx2 = myx2 + 400;
	    					if(myspeed2>4) {
	    						myx2 = myx2 + 300;
	    					}
	    				}
	    				else if((200<(myx2)-(bkx*100*j) && 10200>(myx2)-(bkx*100*j))&&
	    						(1400<(myy2)-(bky*100*i) && 3800>=(myy2)-(bky*100*i))){//닿은곳이 위쪽일때 처리
//	    					System.out.println("위쪽이 닿았다.");
	    					myy2 = myy2 - 400;
	    					if(myspeed>4) {
	    						myy2 = myy2 - 300;
	    					}
	    				}
	    				else if((300<(myx2)-(bkx*100*j) && 10200>(myx2)-(bkx*100*j))&&
	    						(8000<(myy2)-(bky*100*i) && 10000>(myy2)-(bky*100*i))){//닿은곳이 아래쪽일때 처리
//	    					System.out.println("아래쪽이 닿았다.");
	    					myy2 = myy2 + 400;
	    					if(myspeed2>4) {
	    						myy2 = myy2 + 300;
	    					}
	    				}
	                }
	            }
	         }
	    }
	    
	    
	    
	    //플레이어가 아이템에 충돌되면 처리
	    for(int i=0;i<13;i++) {
	    	for(int j=0;j<15;j++) {
	    		if(ItemArray[i][j]!=0) {
	    			if((myx2>=bkx*(j)*100)&&(myy2>=(bky)*(i)*100)&&
		    				(myx2<=bkx*(j+2)*100)&&(myy2<=(bky)*(i+2)*100)) {//아이템에 닿는지 검사
	    				//System.out.printf("I2 [ %d, %d ]\n", ((myx2)-(bkx*100*j)), ((myy2)-(bky*100*i)));
	    				
	    				if(ItemArray[i][j]==1) {//충돌한 아이템이 1(스피드) 이면,
	    					if((3000<(myx2)-(bkx*100*j) && 7000>(myx2)-(bkx*100*j))&& //아이템은 깊게 들어가야 획득됨
	    						((3000<(myy2)-(bky*100*i))&&(7000>(myy2)-(bky*100*i)))){
	    						ItemArray[i][j]=0;// 아이템 삭제
	    						//System.out.println("1");
	    						new effectSound("./music/eatItem.wav");//아이템 획득 사운드
		    					myspeed2++; //속도가 1씩 증가하게 된다.
	    					}
	    					if(myspeed2 > 7) {
	    						myspeed2 = 7; //속도의 최고속도는 7이다.
	    					}
	    					//System.out.printf("2 현재속도 : %d \n",myspeed);
	    				}
	    				
	    				else if(ItemArray[i][j]==2) {//충돌한 아이템이 2(물줄기) 이면,
	    					if((3000<(myx2)-(bkx*100*j) && 7000>(myx2)-(bkx*100*j))&& //아이템은 깊게 들어가야 획득됨
		    						((3000<(myy2)-(bky*100*i))&&(7000>(myy2)-(bky*100*i)))){
	    						//System.out.println("2");
	    						ItemArray[i][j]=0;// 아이템 삭제
	    						if(!(waterLength2>=5))
		    						waterLength2++;
	    						new effectSound("./music/eatItem.wav");//아이템 획득 사운드
	    					}
	    				}
	    				
	    				else if(ItemArray[i][j]==3) {//충돌한 아이템이 3(풍선개수 증가) 이면,
	    					if((3000<(myx2)-(bkx*100*j) && 7000>(myx2)-(bkx*100*j))&& //아이템은 깊게 들어가야 획득됨
		    						((3000<(myy2)-(bky*100*i))&&(7000>(myy2)-(bky*100*i)))){
	    						//System.out.println("3");
	    						ItemArray[i][j]=0;// 아이템 삭제
	    						new effectSound("./music/eatItem.wav");//아이템 획득 사운드
	    						maxBubble2 ++; //물풍선의 최대 개수가 1씩 증가한다.
	    					}
	    					if(maxBubble2 > 10) {
    							maxBubble2 = 10; //물풍선 최대 개수는 10이다.
    						}
    						//System.out.printf("2P 최대풍선개수 : %d \n",maxBubble2);
	    				}
	                }
	            }
	         }
	    }
	    
	    //플레이어 물줄기 검사
	    for(int i=0;i<13;i++) {
	    	for(int j=0;j<15;j++) {
	    		if(WaterArray[i][j]!=0) { 
	    			if((myx2>=bkx*(j)*100)&&(myy2>=(bky)*(i)*100)&&
		    				(myx2<=bkx*(j+2)*100)&&(myy2<=(bky)*(i+2)*100)) {//물줄기에 닿는지 검사
	    				//물줄기는 깊게 들어가야 획득됨 -> 봐서 더 얕게
	    				if((2500<(myx2)-(bkx*100*j) && 8000>(myx2)-(bkx*100*j))&& 
	    						((2500<(myy2)-(bky*100*i))&&(8000>(myy2)-(bky*100*i)))){
	    				//System.out.println("trap-----------------------------------------------------");
	    				mymode2=3;
	    				}
	                }
	            }
	         }
	    }
	    
	}
	//---------------process_MY2 끝---
	
	public void process_BUBBLE() {
		for(int i=0;i<bubble.size();i++) { //플레이어1
			 Bubble buff = (Bubble)bubble.elementAt(i);
			 int x=buff.dis.x/52-1;
			 int y=buff.dis.y/52-1;
			 
			 if(buff.cnt>=2){ //생성뒤 일정시간 지나면
				 MapArray[y][x]=10;
			 }
			 
			 
			 if(buff.cnt==200) { //터지는 속도
				 MapArray[y][x]=0; //다시 이동할 수 있게
				 bubble.remove(i);
				 make_WATER(x,y,buff.waterLength); //물줄기 생성
				 new effectSound("./music/bubbleBoom.wav");//물풍선 터지는 사운드
				 //System.out.println("@@@@@@@@@@");
			 }
			 buff.cnt++;
		}
		
		for(int i=0;i<bubble2.size();i++) { //플레이어2
			 Bubble buff = (Bubble)bubble2.elementAt(i);
			 int x=buff.dis.x/52-1;
			 int y=buff.dis.y/52-1;
			 
			 if(buff.cnt>=2){ //생성뒤 일정시간 지나면
				 MapArray[y][x]=10;
			 }
			 
			 
			 if(buff.cnt==200) { //터지는 속도
				 MapArray[y][x]=0; //다시 이동할 수 있게
				 bubble2.remove(i);
				 make_WATER(x,y,buff.waterLength); //물줄기 생성
				 new effectSound("./music/bubbleBoom.wav");//물풍선 터지는 사운드
				// System.out.println("@@@@@@@@@@");
			 }
			 buff.cnt++;
		}
		
		
		//switch
		
	}
	public void process_WATER() {
		
		for(int i=0;i<water.size();i++) { //3번
			
			Water buff = (Water)water.elementAt(i);
			
			int x = buff.dis.x;
			int y = buff.dis.y;
			
			if(buff.cnt>=25) { //물줄기 제거
				
			water.remove(i);
				
			WaterArray[y][x]=0;
			
			for(int j=0;j<buff.waterLength*4;j++) {
				try {
					switch(j) {
					//상
					case 0: WaterArray[y-1][x]=0; break;
					case 4: WaterArray[y-2][x]=0; break;
					case 8: WaterArray[y-3][x]=0; break;
					case 12: WaterArray[y-4][x]=0; break;
					case 16: WaterArray[y-5][x]=0; break;
				
					//하
					case 1: WaterArray[y+1][x]=0; break;
					case 5: WaterArray[y+2][x]=0; break;
					case 9: WaterArray[y+3][x]=0; break;
					case 13: WaterArray[y+4][x]=0; break;
					case 17: WaterArray[y+5][x]=0; break;
					
					//좌
					case 2: WaterArray[y][x-1]=0; break;
					case 6: WaterArray[y][x-2]=0; break;
					case 10: WaterArray[y][x-3]=0; break;
					case 14: WaterArray[y][x-4]=0; break;
					case 18: WaterArray[y][x-5]=0; break;
					
					//우
					case 3: WaterArray[y][x+1]=0; break;
					case 7: WaterArray[y][x+2]=0; break;
					case 11: WaterArray[y][x+3]=0; break;
					case 15: WaterArray[y][x+4]=0; break;
					case 19: WaterArray[y][x+5]=0; break;
					
					}
				}
				catch(ArrayIndexOutOfBoundsException e){
					
				}
				
			}
				
			}
			buff.cnt++;
			
		}
		
		
		
	}

	
	/////
	public void make_BUBBLE(int x, int y, int waterLength, int from) {
		
		
		if(from == 1) { //사용자 1
			if(bubble.size()>=maxBubble) {
				return;
			}
		}
		else if(from == 2) { //사용자 2
			if(bubble2.size()>=maxBubble2) {
				return;
			}
		}
		
		//버블 개수가 최대면 return
		

		x+=1600;
		x/=100;
		y/=100;
		x = x - (x%52);
		y = y - (y%52)+16;
		if(x<52)
			x=52;
		
		int a=x/52-1;
		int b=y/52-1;
		
		if(a>=15)
			a=14;
		if(b>=13)
			b=12;
		
		
		
		if(MapArray[b][a]!=0)//장애물이 있거나 하면 풍선못만들게
			return;
		else {
			MapArray[b][a]=-1;
			
			if(from==1) {
				Bubble bubbles = new Bubble(x, y,waterLength);
				bubble.add(bubbles);
			}
				
			else if(from==2) {
				Bubble bubbles = new Bubble(x, y,waterLength2);
				bubble2.add(bubbles);
			}
				
			
			
			
			new effectSound("./music/bubbleSet.wav");//물풍선 터지는 사운드
		}

		
	}
	public void make_WATER(int x, int y, int waterLength) {
		
		//System.out.println("test");
		
		// 1 - 가운데 pop
		// 2 - 상 중간, 3 - 상 말단, 4 - 하 중간, 5 - 하 말단
		// 6 - 좌 중간, 7 - 좌 말단, 8 - 우 중간, 8 - 우 말단
		
		Water waters = new Water(x,y,waterLength);
		water.add(waters);
		
		WaterArray[y][x]=1; //물풍선 터진곳
		
		
		
		
		int bufX,bufY;
		//boolean collideCheck;
		
		
		//for(int i=0;i<waterLength;i++) { //3번
		
		int length = waters.waterLength;
			
			for(int j=0;j<4*length;j++) {
				//collide(int bufX, int bufY, Water waters, int index) 
				try {
					switch(j) {			
					case 0: //상1
						bufX=x; bufY=y-1;
						collide(bufX, bufY, waters, j);
						break;
					case 4: //상2
						bufX=x; bufY=y-2;
						if(waters.crash[0]==false) {
							collide(bufX, bufY, waters, j);	
						}
						break;
					case 8: //상3
						bufX=x; bufY=y-3;
						if(waters.crash[4]==false) {
							collide(bufX, bufY, waters, j);
						}
						break;
					case 12: //상4
						bufX=x; bufY=y-4;
						if(waters.crash[8]==false) {
							collide(bufX, bufY, waters, j);
						}
						break;
					case 16: //상5
						bufX=x; bufY=y-5;
						if(waters.crash[12]==false) {
							collide(bufX, bufY, waters, j);
						}
						break;
					case 1: //하1
						bufX=x; bufY=y+1;
						collide(bufX, bufY, waters, j);
						break;
					case 5: //하2
						bufX=x; bufY=y+2;
						if(waters.crash[1]==false) {
							collide(bufX, bufY, waters, j);
						}
					break;
					case 9: //하3
						bufX=x; bufY=y+3;
						if(waters.crash[5]==false) {
							collide(bufX, bufY, waters, j);
					}
					break;
					case 13: //하4
						bufX=x; bufY=y+4;
						if(waters.crash[9]==false) {
							collide(bufX, bufY, waters, j);
					}
					break;
					case 17: //하5
						bufX=x; bufY=y+5;
						if(waters.crash[13]==false) {
							collide(bufX, bufY, waters, j);	
					}
					break;
					case 2: //좌1
						bufX=x-1; bufY=y;
						collide(bufX, bufY, waters, j);
						break;
					case 6: //좌2
						bufX=x-2; bufY=y;
						if(waters.crash[2]==false) {
							collide(bufX, bufY, waters, j);
					}
					break;
					case 10: //좌3
						bufX=x-3; bufY=y;
						if(waters.crash[6]==false) {
							collide(bufX, bufY, waters, j);	
					}
					break;
					case 14: //좌4
						bufX=x-4; bufY=y;
						if(waters.crash[10]==false) {
						collide(bufX, bufY, waters, j);	
					}
					break;
					case 18: //좌5
						bufX=x-5; bufY=y;
						if(waters.crash[14]==false) {
						collide(bufX, bufY, waters, j);	
					}
					break;
					case 3: //우1
						bufX=x+1; bufY=y;
						collide(bufX, bufY, waters, j);	
						break;
					case 7: //우2
						bufX=x+2; bufY=y;
						if(waters.crash[3]==false) {
							collide(bufX, bufY, waters, j);	
					}
						break;
					case 11: //우3
						bufX=x+3; bufY=y;
						if(waters.crash[7]==false) {
							collide(bufX, bufY, waters, j);	
					}break;
					case 15: //우4
						bufX=x+4; bufY=y;
						if(waters.crash[11]==false) {
							collide(bufX, bufY, waters, j);	
					}break;
					case 19: //우5
						bufX=x+5; bufY=y;
						if(waters.crash[15]==false) {
							collide(bufX, bufY, waters, j);	
						}
					break;
					
				}
				}
				catch(ArrayIndexOutOfBoundsException e){
					
				}
			}
		
			
			
			
		//}
		
		//System.out.println(WaterArray[2][2]);
	    
	}
	
	
	public void collide(int bufX, int bufY, Water waters, int index) {//빈공간에만 물줄기 생기게
		

		if(MapArray[bufY][bufX]!=0) {//충돌
			if(index%4==0) { //상단의 경우
				WaterArray[bufY][bufX]=3; //물줄기 생성
				for(int z=index/4;z<5;z++) {
					waters.crash[4*z]=true;
				}
				removeBlock(bufX, bufY);	
			}
			else if(index%4==1) { // 하단의 경우
				WaterArray[bufY][bufX]=5; //물줄기 생성
				for(int z=(index-1)/4;z<5;z++) {
					waters.crash[4*z+1]=true;
				}
				removeBlock(bufX, bufY);
			}
			else if(index%4==2) { // 좌측의 경우
					WaterArray[bufY][bufX]=7; //물줄기 생성
				
				for(int z=(index-2)/4;z<5;z++) {
					waters.crash[4*z+2]=true;
				}
				removeBlock(bufX, bufY);
			}
			else if(index%4==3) { // 우측의 경우
					WaterArray[bufY][bufX]=7; //물줄기 생성
				
				for(int z=(index-3)/4;z<5;z++) {
					waters.crash[4*z+3]=true;
				}
				removeBlock(bufX, bufY);
			}
		}
		else {//그냥 비어있던 경우 - 끝부분은 말단처리 해줘야됨
			
			if(index%4==0) { //상
				index= (index+4)/4; // 1, 2, 3, 4, 5
				if(waters.waterLength == index) { //말단인경우
						WaterArray[bufY][bufX]=3;
				}
				else { //중간 줄기
						WaterArray[bufY][bufX]=2;
						
				}
			}
			else if(index%4==1) { //하
				index= (index+3)/4; // 1, 2, 3, 4, 5
				if(waters.waterLength == index) { //말단인경우
						WaterArray[bufY][bufX]=5;
				}
				else { //중간 줄기
						WaterArray[bufY][bufX]=4;
						
				}
			}
			else if(index%4==2) { //좌
				index= (index+2)/4; // 1, 2, 3, 4, 5
				if(waters.waterLength == index) { //말단인경우
						WaterArray[bufY][bufX]=7;
				}
				else { //중간 줄기
						WaterArray[bufY][bufX]=6;
						
				}
			}
			else if(index%4==3) { //우
				index= (index+1)/4; // 1, 2, 3, 4, 5
				if(waters.waterLength == index) { //말단인경우
						WaterArray[bufY][bufX]=9;
				}
				else { //중간 줄기
						WaterArray[bufY][bufX]=8;
						
				}
			}
				
		}
			
		
		removeBlock(bufX, bufY);
	}
	/*------------------  블럭 파괴 ----------------------*/
	public void removeBlock(int bufX, int bufY) {

		if(!(MapArray[bufY][bufX]==4)&&!(MapArray[bufY][bufX]==5)&&
				!(MapArray[bufY][bufX]==6)&&!(MapArray[bufY][bufX]==7)) {
			MapArray[bufY][bufX]=0;
		}
	}
	
	
	
	
	
	/*------------------   맵   ------------------------*/
	public void MapGenerator(){
		 for(int i=1;i<12;i++) {
	         gamescreen.block[i]=makeImage("./tile/block"+i+".png");
		 }
		Init_MapDATA();
	} 
	
	
	/*------------------   맵 정보   ------------------------*/
	public void Init_MapDATA(){
		bkx=52;//블럭 생성 위치
		bky=52;
	}
	
	
	
	
	
	/*------------------   아이템   ------------------------*/
	public void ItemGenerator(){
		int i;
		for(i=1;i<4;i++) {
			gamescreen.speed[i]=makeImage("./item/speed"+i+".png");
		}
		
		for(i=1;i<4;i++) {
			gamescreen.Bpower[i]=makeImage("./item/Bpower"+i+".png");
		}
		
		for(i=1;i<4;i++) {
			gamescreen.Bmax[i]=makeImage("./item/Bmax"+i+".png");
		}
		
		Init_MapDATA(); //블럭 생성 위치 사용
	}   
	
	
	
	
	
	/////
	
	public Image makeImage(String furl){ //이미지 만들어주는 함수
		Image img;
		Toolkit tk=Toolkit.getDefaultToolkit();
		img=tk.getImage(furl);
		try {
			//여기부터
			MediaTracker mt = new MediaTracker(this);
			mt.addImage(img, 0);
			mt.waitForID(0);
			//여기까지, getImage로 읽어들인 이미지가 로딩이 완료됐는지 확인하는 부분
		} catch (Exception ee) {
			ee.printStackTrace();
			return null; 
		}	
		return img;
	}
	
	
	
}
