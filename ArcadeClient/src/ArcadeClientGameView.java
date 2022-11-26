
// JavaObjClientView.java ObjecStram 기반 Client
//실질적인 채팅 창
import java.awt.BorderLayout;
import java.awt.EventQueue;
import java.awt.FileDialog;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
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

public class ArcadeClientGameView extends JFrame implements KeyListener, Runnable{
	
	//기본 윈도우를 형성하는 프레임을 만든다
	//KeyListener : 키보드 입력 이벤트를 받는다
	//Runnable : 스레드를 가능하게 한다
	
	public static final int UP_PRESSED = 0x001;
    public static final int DOWN_PRESSED = 0x002;
    public static final int LEFT_PRESSED = 0x004;
    public static final int RIGHT_PRESSED = 0x008;
    
    //버블 뜬금없이 생겼던게 조합따라 스페이스바 눌린걸로 판단됐던거같음
    //그래서 20으로 확 올려줌
    public static final int BUBBLE_PRESSED = 0x020;

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
	int keybuff;//키 버퍼값
    
	
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
				{0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 1, 0, 0, 0}, 
				{0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 1, 0, 0, 0},
				{0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 1, 0, 0, 0},
				{0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 1, 0, 0, 0}, 
				{0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 1, 0, 0, 0},
				{0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 1, 0, 0, 0},
				{0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 1, 0, 0, 0}, 
				{0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 1, 0, 0, 0},
				{0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 1, 0, 0, 0},
				{0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0}
		 					};
		int[][] ItemArray = { // 아이템 위치
				//1 : 스피드      2 : 물줄기      3: 풀풍선 최대 개수
				{0, 0, 0, 0, 0, 0, 0, 0, 2, 0, 0, 0, 0, 0, 0},
				{0, 0, 1, 1, 1, 1, 1, 0, 0, 0, 0, 1, 0, 0, 0},
				{0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 2, 0, 0, 0},
				{0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 3, 0, 0, 0},
				{0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 2, 0, 0, 0},
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
	int waterLength = 3; //줄기 길이
	
	//플레이어 이미지
	// 0 wait 1-상 2-하 3-좌 4-우
	int myimg;
	
	
	int mycnt; 
	boolean myshoot=false;//풍선 발사가 눌리고 있는가 
	//int myshield;//실드 남은 수비량 <<아님 봐서 이걸 바늘로..?일단 나중에
	
	boolean my_inv=false;//키보드 반전 ++아이템중에 먹으면 키보드 반전되는 그런거 있지 않았나? 일단 이것도 나중에
	
	
    int gScreenWidth = 1045; //게임 화면 너비
    int gScreenHeight = 785; //게임 화면 높이
    
	Vector bubble=new Vector();//물풍선 관리 - 몇 개 이상 
	Vector water=new Vector();
	
	
	//Vector enemies=new Vector();//적 캐릭터 관리.
	Vector effects=new Vector();//이펙트 관리
	//Vector items=new Vector();//아이템 관리 //얘도 봐서 배열로 빼야되나 
	
	
	//네트워크 관련 변수
	private static final long serialVersionUID = 1L;
	private JPanel contentPane;
	private String UserName;
	private static final int BUF_LEN = 128; // Windows 처럼 BUF_LEN 을 정의
	private Socket socket; // 연결소켓
	private InputStream is;
	private OutputStream os;
	private DataInputStream dis;
	private DataOutputStream dos;

	private ObjectInputStream ois;
	private ObjectOutputStream oos;
	

	//그래픽 관련 변수
	private Frame frame;
	private FileDialog fd;
	private JLabel lblMouseEvent;
	private Graphics gc;
	private int pen_size = 2; // minimum 2
	// 그려진 Image를 보관하는 용도, paint() 함수에서 이용한다.
	private Image panelImage = null; 
	private Graphics gc2 = null;
	
	//private ImageIcon backGround = new ImageIcon("play_bg.png"); //이미지 로딩
	//private Image bG = backGround.getImage(); //이미지 객체 생성

	private Image background=new ImageIcon("play_bg.png").getImage();//배경이미지

	
	/**
	 * Create the frame.
	 * @throws BadLocationException 
	 */
	
	//생성자 --------------------------------------------------
	public ArcadeClientGameView(String username, String ip_addr, String port_no)  {
		
		
		
		//기본적인 윈도우 정보 세팅. 게임과 직접적인 상관은 없이 게임 실행을 위한 창을 준비하는 과정.
		showFrame(); //창 설정
		
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

		MapGenerator();//맵
		ItemGenerator();
		
		status = 2;
		mymode = 2;
		
        UserName = username;
		
		try {
			socket = new Socket(ip_addr, Integer.parseInt(port_no));
//			is = socket.getInputStream();
//			dis = new DataInputStream(is);
//			os = socket.getOutputStream();
//			dos = new DataOutputStream(os);

			oos = new ObjectOutputStream(socket.getOutputStream());
			oos.flush();
			ois = new ObjectInputStream(socket.getInputStream());

		} catch (NumberFormatException | IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			
		}

	} // 생성자 끝 -----------------------------------------------------------
   
	public void systeminit() {//프로그램 초기화
        this.cnt = 0;
        this.delay = 17; // 17/1000초 = 58 (프레임/초)
        this.keybuff = 0;
        
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
                     this.pretime = System.currentTimeMillis();
                     this.gamescreen.repaint();//화면 리페인트
                     this.process();//각종 처리
                     this.keyprocess();//키 처리
                     
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
	
 	//=================================================================================
    
	class ListenNetwork extends Thread { //네트워크 관련 스레드 -------------------
		public void run() {
			while (true) {
				try {

					Object obcm = null;
					String msg = null;
					ChatMsg cm;
					try {
						obcm = ois.readObject();
					} catch (ClassNotFoundException e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
						break;
					}
					if (obcm == null)
						break;
					if (obcm instanceof ChatMsg) {
						cm = (ChatMsg) obcm;
						msg = String.format("[%s]\n%s", cm.UserName, cm.data);
					} else
						continue;
					
				} catch (IOException e) {
					//AppendText("ois.readObject() error");
					try {
//						dos.close();
//						dis.close();
						ois.close();
						oos.close();
						socket.close();

						break;
					} catch (Exception ee) {
						break;
					} // catch문 끝
				} // 바깥 catch문끝

			}
		}
	}//-----------------------------------------------------------------------------
	
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

	
	// 키 이벤트 리스너 처리
	@Override
	public void keyPressed(KeyEvent e) { //키보드를 눌렀을 때 호출, 모든 키보드에 반응
		if(status==2){
			switch(e.getKeyCode()){
			case KeyEvent.VK_SPACE:
				keybuff|=BUBBLE_PRESSED;
				break;
			case KeyEvent.VK_LEFT:
				keybuff|=LEFT_PRESSED; //멀티키의 누르기 처리
				break;
			case KeyEvent.VK_UP:
				keybuff|=UP_PRESSED;
				break;
			case KeyEvent.VK_RIGHT:
				keybuff|=RIGHT_PRESSED;
				break;
			case KeyEvent.VK_DOWN:
				keybuff|=DOWN_PRESSED;
				break;
			default:
				break;
			}
		} else if(status!=2) keybuff=e.getKeyCode();
		
	}


	@Override
	public void keyReleased(KeyEvent e) { //키보드를 떼었을 때, 모든 키보드에 반응
		switch(e.getKeyCode()){
		case KeyEvent.VK_SPACE:
			//키보드 뗄때는 물풍선 안 만들어줘도 되지 않나
			keybuff&=~BUBBLE_PRESSED;
			myshoot=true;
			break;
		case KeyEvent.VK_LEFT:
			keybuff&=~LEFT_PRESSED;//멀티키의 떼기 처리
			break;
		case KeyEvent.VK_UP:
			keybuff&=~UP_PRESSED;
			break;
		case KeyEvent.VK_RIGHT:
			keybuff&=~RIGHT_PRESSED;
			break;
		case KeyEvent.VK_DOWN:
			keybuff&=~DOWN_PRESSED;
			break;
		}
			//PC 환경에서는 기본적으로 키보드의 반복입력을 지원하지만,
			//그렇지 않은 플랫폼에서는 키 버퍼값에 떼고 눌렀을 때마다 값을 변경해 리피트 여부를 제어한다.
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
			break;
		case 2://playing
			process_MY();
			process_BUBBLE();
			process_WATER();
			
			//process_ENEMY();
			 							//process_BUBBLE();
			//process_EFFECT();
			//process_GAMEFLOW();
			//process_ITEM();
			break;
		case 3://게임오버
//			process_ENEMY();
//			process_BULLET();
//			process_GAMEFLOW();
			break;
		case 4://일시정지
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
    						make_BUBBLE(myx,myy);
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
    						make_BUBBLE(myx,myy);
    					}
    					mydegree=0;
    					myimg=1;
    					break;
    				
    				case LEFT_PRESSED|BUBBLE_PRESSED:
    					if(gamecnt%bubbleCnt==0) { //gamecnt%bubbleCnt==0
    						make_BUBBLE(myx,myy);
    					}
    					mydegree=90;
    					myimg=3;
    					break;
    				
    				case RIGHT_PRESSED|BUBBLE_PRESSED:
    					if(gamecnt%bubbleCnt==0) { //gamecnt%bubbleCnt==0
    						make_BUBBLE(myx,myy);
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
    						make_BUBBLE(myx,myy);
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
    						make_BUBBLE(myx,myy);
    					}
    					mydegree=0;
    					myimg=1;
    					break;
    				
    				case DOWN_PRESSED|BUBBLE_PRESSED:
    					if(gamecnt%bubbleCnt==0) {
    						make_BUBBLE(myx,myy);
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
    						make_BUBBLE(myx,myy);
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
    						make_BUBBLE(myx,myy);
    					}
    					mydegree=180;
    					myimg=2;
    					break;
    				default:
    					System.out.println(""+keybuff);
    					keybuff=0;
    					mydegree=-1;
    					//myimg=0;
    					break;
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
				gamescreen.chrTrap[i]=makeImage("./player/trap1"+i+".png"); //trap10 11 12 
		}
		Init_MYDATA();
	}
	public void Init_MYDATA(){
		
		//플레이어의 x,y는 *100된 상태
		//리스폰 장소
		myx=5000;
		myy=17000; 
		
		myspeed=4; //속도
		mydegree=-1; //방향
		//mywidth, myheight;//플레이어 캐릭터의 너비 높이
		mymode=2; //온플레이
		myimg=2;
		mycnt=0;
		keybuff=0;
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
			myx+=200;
			if(myx>20000) mymode=2;
			break;
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
//			if(myimg==6) {
//				myx-=20;
//				if(cnt%4==0||myshoot){
//					myshoot=false;
//					shoot=new Bullet(myx+2500, myy+1500, 0, 0, RAND(245,265), 8);
//					bullets.add(shoot);
//					shoot=new Bullet(myx+2500, myy+1500, 0, 0, RAND(268,272), 9);
//					bullets.add(shoot);
//					shoot=new Bullet(myx+2500, myy+1500, 0, 0, RAND(275,295), 8);
//					bullets.add(shoot);
//				}
//				//8myy+=70;
//			}
			break;
		case 3:
			//keybuff=0;
			myimg=8;
			if(mycnt--==0) {
				mymode=0;
				mycnt=50;
			}
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
	    			if((myx>=bkx*(j)*100)&&(myy>=(bky+1)*(i)*100)&&
	    				(myx<=bkx*(j+2)*100)&&(myy<=(bky)*(i+2)*100)) {//물체에 닿는지 검사	    				
	    				System.out.printf("B [ %d, %d ]\n", ((myx)-(bkx*100*j)), 
	    						((myy)-(bky*100*i)));
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
	    				System.out.printf("I [ %d, %d ]\n", ((myx)-(bkx*100*j)), 
	    						((myy)-(bky*100*i)));
	    				
	    				if(ItemArray[i][j]==1) {//충돌한 아이템이 1(스피드) 이면,
	    					if((3000<(myx)-(bkx*100*j) && 7000>(myx)-(bkx*100*j))&& //아이템은 깊게 들어가야 획득됨
	    						((3000<(myy)-(bky*100*i))&&(7000>(myy)-(bky*100*i)))){
	    						ItemArray[i][j]=0;// 아이템 삭제
	    						//System.out.println("1");
	    						new effectSound("./music/eatProp.wav");//아이템 획득 사운드
		    					myspeed++; //속도가 1씩 증가하게 된다.
	    					}
	    					if(myspeed > 7) {
	    						myspeed = 7; //속도의 최고속도는 7이다.
	    					}
	    					System.out.printf("현재속도 : %d \n",myspeed);
	    				}
	    				
	    				else if(ItemArray[i][j]==2) {//충돌한 아이템이 2(물줄기) 이면,
	    					if((3000<(myx)-(bkx*100*j) && 7000>(myx)-(bkx*100*j))&& //아이템은 깊게 들어가야 획득됨
		    						((3000<(myy)-(bky*100*i))&&(7000>(myy)-(bky*100*i)))){
	    						//System.out.println("2");
	    						ItemArray[i][j]=0;// 아이템 삭제
	    						new effectSound("./music/eatProp.wav");//아이템 획득 사운드
	    					}
	    				}
	    				
	    				else if(ItemArray[i][j]==3) {//충돌한 아이템이 3(풍선개수 증가) 이면,
	    					if((3000<(myx)-(bkx*100*j) && 7000>(myx)-(bkx*100*j))&& //아이템은 깊게 들어가야 획득됨
		    						((3000<(myy)-(bky*100*i))&&(7000>(myy)-(bky*100*i)))){
	    						//System.out.println("3");
	    						ItemArray[i][j]=0;// 아이템 삭제
	    						new effectSound("./music/eatProp.wav");//아이템 획득 사운드
	    						maxBubble ++; //물풍선의 최대 개수가 1씩 증가한다.
	    					}
	    					if(maxBubble > 10) {
    							maxBubble = 10; //물풍선 최대 개수는 10이다.
    						}
    						System.out.printf("최대풍선개수 : %d \n",maxBubble);
	    				}
	                }
	            }
	         }
	    }
	    
	}//---------------process_MY 끝---
	
	public void process_BUBBLE() {
		
		//System.out.println(bubble.size());

		
		for(int i=0;i<bubble.size();i++) {
			 Bubble buff = (Bubble)bubble.elementAt(i);
			 int x=buff.dis.x/52-1;
			 int y=buff.dis.y/52-1;
			 
			 
			 if(buff.cnt==100) { //터지는 속도
				 MapArray[y][x]=0; //다시 이동할 수 있게
				 bubble.remove(i);
				 make_WATER(x,y, buff.from); //물줄기 생성
				 new effectSound("./music/bubbleBoom.wav");//물풍선 터지는 사운드
				 System.out.println("@@@@@@@@@@");
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
//			System.out.println("+++++++++");
//			System.out.println(x);
//			System.out.println(y);
			
			if(buff.cnt>=25) { //물줄기 제거
				
			water.remove(i);
				
			WaterArray[y][x]=0;
			
			for(int j=0;j<12;j++) {
				try {
					switch(j) {
						case 0: WaterArray[y-1][x]=0; break;
						case 1: WaterArray[y-2][x]=0; break;
						case 2: WaterArray[y-3][x]=0; break;
						case 3: WaterArray[y+1][x]=0; break;
						case 4: WaterArray[y+2][x]=0; break;
						case 5: WaterArray[y+3][x]=0; break;
						case 6: WaterArray[y][x-1]=0; break;
						case 7: WaterArray[y][x-2]=0; break;
						case 8: WaterArray[y][x-3]=0; break;
						case 9: WaterArray[y][x+1]=0; break;
						case 10: WaterArray[y][x+2]=0; break;
						case 11: WaterArray[y][x+3]=0; break;
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
	public void make_BUBBLE(int x, int y) {
		
		//버블 개수가 최대면 return
		if(bubble.size()>=maxBubble)
			return;
		
		x/=100;
		y/=100;
		//버블 위치 조정
		x = x - (x%52);
		y = y - (y%52)+16;
		if(x<52)
			x=52;
		
		int a=x/52-1;
		int b=y/52-1;
		
		System.out.println(a);
		System.out.println(b);
		
		if(MapArray[b][a]!=0)//장애물이 있거나 하면 풍선못만들게
			return;
		else {
			MapArray[b][a]=-1;
			Bubble bubbles = new Bubble(x, y, 1);
			bubble.add(bubbles);
			new effectSound("./music/bubbleSet.wav");//물풍선 터지는 사운드
		}
		
//		if(MapArray[b][a]==0) {
//			
//		}
//			MapArray[b][a]=-1;
//		
//		
		
		//버블이 있는 곳은 Map을 -1로 해놓음
		 
		 
 		 System.out.println(x);
 		System.out.println(y);
		 
		 System.out.println("++++++++++++++++++++++");
		 System.out.println(x);
		 System.out.println(y);
		 System.out.println("++++++++++++++++++++++");
		 
		 //if(MapArray[b][a]!=0) 
		 //{ 
			 //bubble.remove(bubble.size()-1);
//			 System.out.println("++++++++++++++++++++++");
//			 System.out.println(MapArray[b][a]);
//			 System.out.println(MapArray[b][a+1]);
//			 System.out.println(MapArray[b+1][a]);
//			 System.out.println(MapArray[b+1][a+1]);
			 //MapArray[b][a]=-1;
		//}
			 
//		 System.out.println("=========================");
//		 System.out.println(MapArray[b][a]);
//		 System.out.println(MapArray[b][a+1]);
//		 System.out.println(MapArray[b+1][a]);
//		 System.out.println(MapArray[b+1][a+1]);
//		 System.out.println("=========================");
//		 System.out.println(MapArray[b][a]);
//		 System.out.println(bubbles.dis);
//		 System.out.println(b);
//		 System.out.println(a);
//		 System.out.println("=========================끝");
		
		//make_BUBBLE(myx,myy);
		//bubble.add(bubbles);
		
		//Bubble bubbles = new Bubble(x, y, 1);
		
		
	}
	public void make_WATER(int x, int y, int from) {
		
		System.out.println("test");
		
		// 1 - 가운데 pop
		// 2 - 상 중간, 3 - 상 말단, 4 - 하 중간, 5 - 하 말단
		// 6 - 좌 중간, 7 - 좌 말단, 8 - 우 중간, 8 - 우 말단
		
		Water waters = new Water(x,y,waterLength, from);
		water.add(waters);
		
		WaterArray[y][x]=1; //물풍선 터진곳
		
		int bufX,bufY;
		boolean collideCheck;
		
		
		//for(int i=0;i<waterLength;i++) { //3번
			
			for(int j=0;j<12;j++) {
				
				
				System.out.println(j);
				try {
					switch(j) {
//						case 0: //상1
//						case 1:
//						case 2:
//							bufX=x; bufY=y-1;
//							if(!collide(bufX, bufY, waters)){ //블럭이나 사람을 만난경우
//								waters.crash[1]=true;
//								break;
//							}
//							//
//							if(waters.crash[1]==false) {
//								System.out.println("////////////////");
//								bufX=x; bufY=y-2;
//								if(!collide(bufX, bufY, waters)){ //블럭이나 사람을 만난경우
//									waters.crash[2]=true;
//									//j+=1; //다음 줄기 건너뜀
//								}
//								break;
//							}
//							if(waters.crash[2]==false) {
//								System.out.println(waters.crash[2]);
//								bufX=x; bufY=y-3;
//								if(!collide(bufX, bufY, waters )){ //블럭이나 사람을 만난경우
//									
//								}
//								break;
//							}
							//
//						case 1: //상2
//							if(waters.crash[1]==false) {
//								System.out.println("////////////////");
//								bufX=x; bufY=y-2;
//								if(!collide(bufX, bufY, waters)){ //블럭이나 사람을 만난경우
//									waters.crash[2]=true;
//									//j+=1; //다음 줄기 건너뜀
//								}
//								break;
//							}
//							
//						case 2: //상3
					
					
//						case 0: //상1
//							bufX=x; bufY=y-1;
//							if(!collide(bufX, bufY, waters)){ //블럭이나 사람을 만난경우
//								waters.crash[1]=true;
//								removeBlock(bufX, bufY);
//								
//							}
//							break;
//						case 1: //상2
//							bufX=x; bufY=y-2;
//							if(waters.crash[1]==false) {
//								if(!collide(bufX, bufY, waters)){ //블럭이나 사람을 만난경우
//									waters.crash[2]=true;
//									removeBlock(bufX, bufY);
//							}
//								
//						}
//						break;
//						case 2: //상3
//							bufX=x; bufY=y-3;
//							if(waters.crash[2]==false) {
//								if(!collide(bufX, bufY, waters)){ //블럭이나 사람을 만난경우
//									removeBlock(bufX, bufY);
//							}
//								
//						}
//						break;
	//----------------------------------------------------------------------------------------					
					case 0: //상1
						bufX=x; bufY=y-1;
						if(!collide(bufX, bufY)){ //블럭이나 사람을 만난경우
							waters.crash[1]=true;
							waters.crash[2]=true;
							removeBlock(bufX, bufY);
							
						}
						break;
					case 1: //상2
						bufX=x; bufY=y-2;
						if(waters.crash[1]==false) {
							if(!collide(bufX, bufY)){ //블럭이나 사람을 만난경우
								waters.crash[2]=true;
								removeBlock(bufX, bufY);
						}
								
					}
						break;
					case 2: //상3
						bufX=x; bufY=y-3;
						if(waters.crash[2]==false) {
							if(!collide(bufX, bufY)){ //블럭이나 사람을 만난경우
								removeBlock(bufX, bufY);
						}}
						
						break;
	
						case 3: //하1
							bufX=x; bufY=y+1;
							if(!collide(bufX, bufY)){ //블럭이나 사람을 만난경우
								waters.crash[4]=true;
								waters.crash[5]=true;
								removeBlock(bufX, bufY);
								
							}
							break;
						case 4: //하2
							bufX=x; bufY=y+2;
								if(waters.crash[4]==false) {
									if(!collide(bufX, bufY)){ //블럭이나 사람을 만난경우
										waters.crash[5]=true;
										removeBlock(bufX, bufY);
								}
									
							}
							break;
						case 5: //하3
							bufX=x; bufY=y+3;
							if(waters.crash[5]==false) {
								if(!collide(bufX, bufY)){ //블럭이나 사람을 만난경우
//									System.out.println("TTTTTTTTTTTTTTTTTTT");
									removeBlock(bufX, bufY);
							}}
							
							break;
							
						case 6: //좌1
							bufX=x-1; bufY=y;
							if(!collide(bufX, bufY)){ //블럭이나 사람을 만난경우
								waters.crash[7]=true;
								waters.crash[8]=true;
								removeBlock(bufX, bufY);
								
							}
							break;
						case 7: //좌2
							bufX=x-2; bufY=y;
								if(waters.crash[7]==false) {
									if(!collide(bufX, bufY)){ //블럭이나 사람을 만난경우
										waters.crash[8]=true;
										removeBlock(bufX, bufY);
								}
									
							}
							break;
						case 8: //좌3
							bufX=x-3; bufY=y;
							if(waters.crash[8]==false) {
								if(!collide(bufX, bufY)){ //블럭이나 사람을 만난경우
									removeBlock(bufX, bufY);
							}}
							
							break;
							
						case 9: //우1
							bufX=x+1; bufY=y;
							if(!collide(bufX, bufY)){ //블럭이나 사람을 만난경우
								waters.crash[10]=true;
								waters.crash[11]=true;
								removeBlock(bufX, bufY);
								
							}
							break;
						case 10: //우2
							bufX=x+2; bufY=y;
								if(waters.crash[10]==false) {
									if(!collide(bufX, bufY)){ //블럭이나 사람을 만난경우
										waters.crash[11]=true;
										removeBlock(bufX, bufY);
								}
									
							}
							break;
						case 11: //우3
							bufX=x+3; bufY=y;
							if(waters.crash[11]==false) {
								if(!collide(bufX, bufY)){ //블럭이나 사람을 만난경우
									removeBlock(bufX, bufY);
							}}
							
							break;

//----------------------------------------------------------------------------------------	
							
//						case 6: //좌1
							
							
//							bufX=x-1; bufY=y;
//							if(!collide(bufX, bufY)){ //블럭이나 사람을 만난경우
//								j+=2; //다음 줄기 건너뜀
//							}
//							break;
//						case 7: //좌2
//							bufX=x-2; bufY=y;
//							if(!collide(bufX, bufY)){ //블럭이나 사람을 만난경우
//								j+=1; //다음 줄기 건너뜀
//							}
//							break;
//						case 8: //좌3
//							bufX=x-3; bufY=y;
//							if(!collide(bufX, bufY)){ //블럭이나 사람을 만난경우
//								
//							}
//							break;
//						case 9: //우1
//							bufX=x+1; bufY=y;
//							if(!collide(bufX, bufY)){ //블럭이나 사람을 만난경우
//								j+=2; //다음 줄기 건너뜀
//							}
//							break;
//						case 10: //우2
//							bufX=x+2; bufY=y;
//							if(!collide(bufX, bufY)){ //블럭이나 사람을 만난경우
//								j+=1; //다음 줄기 건너뜀
//							}
//							break;
//						case 11: //우3
//							bufX=x+3; bufY=y;
//							if(!collide(bufX, bufY)){ //블럭이나 사람을 만난경우
//								
//							}
//							break;
					}
				}
				catch(ArrayIndexOutOfBoundsException e){
					
				}
			}
		//}
		
		//System.out.println(WaterArray[2][2]);
	    
	}
	
	
	public boolean collide(int bufX, int bufY) {//빈공간에만 물줄기 생기게
		
		if(MapArray[bufY][bufX]==0) { //빈공간이면
			WaterArray[bufY][bufX]=2; //물줄기 생성
			return true;
		}
		else {//아니라면
			// 0 - 빈블럭, 1 - 박스, 2 - 빨강블럭, 3 - 주황블럭, 4 - 주황집
			// 5 - 노랑집, 6 - 파랑집, 7 - 나무, 8 - 풀
//			if(!(MapArray[bufY][bufX]==4))
//				MapArray[bufY][bufX]=0; //블럭 파괴
			
			return false;
		}
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
