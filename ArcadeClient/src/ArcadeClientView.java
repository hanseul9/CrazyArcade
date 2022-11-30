import java.awt.Color;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.Graphics;
import java.awt.Image;
import java.awt.Toolkit;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.OutputStream;
import java.net.Socket;

import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.SwingConstants;
import javax.swing.border.EmptyBorder;



public class ArcadeClientView extends JFrame {
	
	ArcadeClientView clientView = this;
	
	int maxRoomCnt=4;
	RoomBox roombox[] = new RoomBox[maxRoomCnt];
	
	
	
	//private JPanel contentPane;
	private String UserName;
	
	//네트워크 관련 변수
	private static final long serialVersionUID = 1L;
	private static final int BUF_LEN = 128; // Windows 처럼 BUF_LEN 을 정의
	private Socket socket; // 연결소켓
	private InputStream is;
	private OutputStream os;
	private DataInputStream dis;
	private DataOutputStream dos;

	private ObjectInputStream ois;
	private ObjectOutputStream oos;
	
	private JPanel contentPane;
	JScrollPane scrollPane;
	
	
	public ArcadeClientView(String username, String ip_addr, String port_no) { //생성자
		ImageIcon bg = new ImageIcon("./roomIMG/roomBG.png");//배경화면
		ImageIcon createRoomBTN = new ImageIcon("./roomIMG/createRoomBTN.png");
		ImageIcon createRoomBTN2 = new ImageIcon("./roomIMG/createRoomBTN2.png");
		ImageIcon RoomBG = new ImageIcon("./roomIMG/roomIMG.png");//방 패널 이미지
		
		this.UserName = username;
		//System.out.println("ArcadeClientView");
		
		//JFrame-------------------------------
		setTitle("대기실"); //프레임 타이틀 지정
		//setSize(450,700);//프레임 크기
		setResizable(true); //창크기 변경불가
		setLocationRelativeTo(null);//창 가운데 뜨게
		setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		getContentPane().setLayout(null);
		
		contentPane = new JPanel() {//배경화면 설정
			 public void paintComponent(Graphics g) {
	                g.drawImage(bg.getImage(), 0, 0, null);
	            }
		};
		scrollPane = new JScrollPane(contentPane);
		setContentPane(scrollPane);
		
		contentPane.setBorder(new EmptyBorder(5, 5, 5, 5));
		setContentPane(contentPane);
		contentPane.setLayout(null);
		
		
		//방 만들기 버튼------------------------
		JButton makeRoomButton = new JButton(createRoomBTN);
		makeRoomButton.setRolloverIcon(createRoomBTN2);//버튼에 마우스 올라가면 이미지 변경
		makeRoomButton.setBorderPainted(false);// 버튼 테두리 설정해제
		makeRoomButton.setBounds(145, 380, 130, 45);
		contentPane.add(makeRoomButton);
		
		
		
		setVisible(true);
		setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		setBounds(500, 100, 435, 500);//창크기, 위치 조절
		
		//JPanel---------------------------------
		
		
		Myaction action = new Myaction();
		makeRoomButton.addActionListener(action);
		
		
		for(int i=0;i<maxRoomCnt;i++) {

			roombox[i] = new RoomBox();
			
			switch(i) {
			case 0:
				roombox[i].panel.setLocation(30,30);break;
				
				
			case 1:
				roombox[i].panel.setLocation(220,30);break;
			case 2:
				roombox[i].panel.setLocation(30,200);break;
			case 3:
				roombox[i].panel.setLocation(220,200);break;
			}
			
		}
		
		UserName = username;
		
		try {
			socket = new Socket(ip_addr, Integer.parseInt(port_no));
			oos = new ObjectOutputStream(socket.getOutputStream());
			oos.flush();
			ois = new ObjectInputStream(socket.getInputStream());
			
			
			// ChatMsg parameter - UserName, 프로토콜, 메세지
			// 로그인
			ChatMsg obcm = new ChatMsg(UserName, "100", "Hello");
			SendObject(obcm); //전송
			
			ListenNetwork net = new ListenNetwork();
			net.start();


		} catch (NumberFormatException | IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			
		}
				
		
		
		
	} //생성자 끝---------------------------------------------------
	
	
	public void SendObject(Object ob) { // 서버로 메세지를 보내는 메소드
		try {
			oos.writeObject(ob);
		} catch (IOException e) {
			// textArea.append("메세지 송신 에러!!\n");
			//AppendText("SendObject Error");
		}
	}
	
	// Server Message를 수신
	class ListenNetwork extends Thread {
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
					
					
					
					switch (cm.code) {
					
					case "300": //방 정보 출력

						//공백을 기준으로 나눔 - "방제목" "RoomId" 형태
						String[] roomInfo = cm.data.split("\\+     \\+");
						int roomId = Integer.parseInt(roomInfo[1]);
						
						roombox[roomId].roomTitle.setText(roomInfo[0]);
						break;
					case "500", "501", "502", "503": //방 입장 허가 프로토콜
						
						
						break;
					
					
					}
				} catch (IOException e) {
					System.out.println("ois.readObject() error");
					try {
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
	}
	
	class Myaction implements ActionListener // 내부클래스로 액션 이벤트 처리 클래스
	{
		@Override
		public void actionPerformed(ActionEvent e) {
			
			MakeRoomView view = new MakeRoomView(clientView, UserName);

			
//			String username = txtUserName.getText().trim();
//			String ip_addr = txtIpAddress.getText().trim();
//			String port_no = txtPortNumber.getText().trim();
//			ArcadeClientView view = new ArcadeClientView(username, ip_addr, port_no);
			
			//ArcadeClientGameView view = new ArcadeClientGameView(username, ip_addr, port_no);
			//setVisible(false);
		}
	}

	
	
	class RoomBox{ //사용자에게 보여지는 방 목록
		ImageIcon roomBG = new ImageIcon("./roomIMG/roomIMG.png"); 
		ImageIcon connectRoomBTN = new ImageIcon("./roomIMG/connectRoomBTN.png");
		ImageIcon connectRoomBTN2 = new ImageIcon("./roomIMG/connectRoomBTN2.png");
		
		public JPanel panel = new JPanel();
		JLabel imgLabel = new JLabel();
		JLabel roomTitle = new JLabel(); //방제목
		
		Font font = new Font("맑은 고딕", Font.BOLD, 19);//폰트만들기
		
	    public RoomBox() {
	    	
	        panel.setSize(170,150);//방 패널 사이즈 (이미지 사이즈와 동일)
	        panel.setLayout(null);
	        
	        //방제목을 보여줌
	        roomTitle.setSize(130,45); 
	        roomTitle.setText("빈 방");//기본 빈방
	        roomTitle.setFont(font);//폰트적용
	        roomTitle.setForeground(Color.WHITE);//폰트색상
	        roomTitle.setLocation(20, 20); //제목 위치
	        roomTitle.setHorizontalAlignment(JLabel.CENTER);//가운데 정렬
	        panel.add(roomTitle);
	        
	        
	        
	        JButton enter = new JButton(connectRoomBTN);//입장버튼
	        enter.setRolloverIcon(connectRoomBTN2);//버튼에 마우스 올라가면 이미지 변경
	        enter.setBorderPainted(false);// 버튼 테두리 설정해제
	        enter.setSize(130,45);//버튼사이즈 조절
	        enter.setLocation(21,75);//버튼 위치 조절
	        panel.add(enter);
	        
	        
	        
	        imgLabel.setIcon(roomBG);//이미지
	        imgLabel.setSize(170,150);//방 배경 이미지 사이즈
	        imgLabel.setLocation(0,0);
	        panel.add(imgLabel);
	         
	        getContentPane().add(panel);
	        //roomBox[i].setVisible(true);
	     }
	}
	
}