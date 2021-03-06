package umich.eecs285.towerdefence;

import java.io.IOException;

public class TowerDefenseGame extends Thread implements TowerDefensedataArray {
	public static long Preparation_Time = 15000;
	public static int delay = 30;

	private Controller control;
	private TowerDefenseDataBase DB;
	private ClientBridge client_bridge;
	private MainFrame mainFrame;
	private Player player;
	private int turn;
	private long timestamp;
	private BackgroundMusic backgroundMusic;
	private TowerDefense_TransData towerDefense_TransData;
	private TowerDefense_TransData opponentData;
	private TowerDefense_TransData receivedData = null;
	private boolean draw_state = false;
	private Messager messager;
	private byte clientId;

	public static void main(String args[]) {

		TowerDefenseGame game = new TowerDefenseGame();
		game.initConnection();

		while (!game.checkInitalBridge()) {
			try {
				sleep(delay);
			} catch (InterruptedException e) {
				e.printStackTrace();
			}
			System.out.println("Try Create");
		}

	}

	public void setClientId(byte clientId) {
		this.clientId = clientId;
	}

	public void initConnection() {
		this.mainFrame = new MainFrame();
		mainFrame.turnOnInput();
		this.client_bridge = new ClientBridge();
		this.mainFrame.setClientBridge(client_bridge);
		backgroundMusic = new BackgroundMusic();
		backgroundMusic.startWelcome();
	}

	private void init() {
		messager = new Messager(clientId);
		// TODO change client side initialization to get ip from player
		mainFrame.start();
		if (clientId == Messager.Id_Server) {
			messager.initialization();
			mainFrame.turnOnRound("Wait player to join: "
					+ messager.getServerIp());
		} else {
			String serverIp = client_bridge.getIp();
			messager.initialization(serverIp);
		}
		player = new Player();
		turn = 1;
		control = new Controller();
		control.init();
		DB = new TowerDefenseDataBase();
		DB.init();
		receivedData = null;
	}

	private void setTimestamp() {
		timestamp = System.currentTimeMillis();
	}

	public void run() {
		init();
		backgroundMusic.stopWelcome();
		if (clientId == Messager.Id_Server) {
			backgroundMusic.startBattle();
		}
		for (; turn < 9; turn++) {
			// Round start
			setTimestamp();
			while (!messager.ifNextRoundReady()) {
				System.out.println("Client: Wait for NextRoundReady "
						+ System.currentTimeMillis());
				try {
					sleep(delay); // wait delay ms
				} catch (InterruptedException e) {
					e.printStackTrace();
				}
				setTimestamp();
				checkRunBridge();
				messager.transmitRoundReady();
				if (messager.getReceivedData().getTransmitType() == Transmit_Type_Game_End) {
					mainFrame.turnOnRound("You Win!");
					try {
						sleep(10000);
					} catch (InterruptedException e) {
						e.printStackTrace();
					}
					System.exit(0);
				} else if (messager.getReceivedData().getTransmitType() == Transmit_Type_Regular)
					opponentData = messager.getReceivedData();
				else if (messager.getReceivedData().getSize() > 0)
					receivedData = messager.getReceivedData();
				// paint data
				if (draw_state == false && towerDefense_TransData != null)
					mainFrame.nextFrame(towerDefense_TransData);
				if (draw_state == true && opponentData != null)
					mainFrame.nextFrame(opponentData);
			}
			if (clientId == Messager.Id_Server)
				mainFrame.turnOffRound();
			long nextRoundStartTime = messager.getNextRoundStartTime();
			// wait till nextRoundStartTime
			mainFrame.turnOnRound("Round" + turn);
			while (System.currentTimeMillis() < nextRoundStartTime) {
				System.out.println("Client: wait till nextRoundStartTime "
						+ System.currentTimeMillis());
				try {
					sleep(delay);
				} catch (InterruptedException e) {
					e.printStackTrace();
				}
			}
			mainFrame.turnOffRound();

			// Preparation time
			while (System.currentTimeMillis() < nextRoundStartTime
					+ Preparation_Time) {
				System.out.println("Client: round start! "
						+ System.currentTimeMillis());
				setTimestamp();
				checkprepBridge();
				towerDefense_TransData = control.getInfo(clientId, timestamp);
				System.out.println(towerDefense_TransData.toString());
				towerDefense_TransData.setTransmitType(Transmit_Type_Regular);
				messager.transmitRegularData(towerDefense_TransData);
				if (messager.getReceivedData().getTransmitType() == Transmit_Type_Game_End) {
					mainFrame.turnOnRound("You Win!");
					try {
						sleep(10000);
					} catch (InterruptedException e) {
						e.printStackTrace();
					}
					System.exit(0);
				} else if (messager.getReceivedData().getTransmitType() == Transmit_Type_Regular)
					opponentData = messager.getReceivedData();
				else if (messager.getReceivedData().getSize() > 0)
					receivedData = messager.getReceivedData();
				try {
					sleep(delay);
				} catch (InterruptedException e) {
					e.printStackTrace();
				}
				// paint data
				if (draw_state == false)
					mainFrame.nextFrame(towerDefense_TransData);
				if (draw_state == true)
					mainFrame.nextFrame(opponentData);

			}

			// Running

			if (receivedData != null) {
				try {
					System.out.println(JSONUtility.arrayToJSON(receivedData));
				} catch (IOException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
				int attackingUnits[] = new int[receivedData.getSize()];
				for (int i = 0; i < receivedData.getSize(); i++) {
					attackingUnits[i] = receivedData.TowerDefense_TransArray[i]
							.getId();
				}
				control.startTurn(turn, attackingUnits.length, attackingUnits);
				receivedData = null;
			} else {
				control.startTurn(turn, 0, null);
			}
			player.clearAttackingId();
			while (!control.isEnd()) {
				setTimestamp();
				checkRunBridge();
				control.run();
				towerDefense_TransData = control.getInfo(clientId, timestamp);
				towerDefense_TransData.setTransmitType(Transmit_Type_Regular);
				messager.transmitRegularData(towerDefense_TransData);
				if (messager.getReceivedData().getTransmitType() == Transmit_Type_Game_End) {
					mainFrame.turnOnRound("You Win!");
					try {
						sleep(10000);
					} catch (InterruptedException e) {
						e.printStackTrace();
					}
					System.exit(0);
				} else if (messager.getReceivedData().getTransmitType() == Transmit_Type_Regular)
					opponentData = messager.getReceivedData();
				else if (messager.getReceivedData().getSize() > 0)
					receivedData = messager.getReceivedData();

				try {
					sleep(delay);
				} catch (InterruptedException e) {
					e.printStackTrace();
				}
				// paint
				if (draw_state == false)
					mainFrame.nextFrame(towerDefense_TransData);
				if (draw_state == true)
					mainFrame.nextFrame(opponentData);

				System.out.println(towerDefense_TransData.toString());
				if (control.isDead()) {
					cushion();
					for (int i = 0; i < 10; i++) {
						mainFrame.turnOnRound("You Lose!");
						try {
							sleep(delay);
						} catch (InterruptedException e1) {
							// TODO Auto-generated catch block
							e1.printStackTrace();
						}
						messager.transmitGameEnd();
					}
					try {
						sleep(10000);
					} catch (InterruptedException e) {
						e.printStackTrace();
					}
					System.exit(0);
					break;
				}
			}

			// round end
			System.out.println("Client: round end "
					+ System.currentTimeMillis());
			cushion();
			control.endTurn();
			cushion();
			messager.transmitRoundReady(player.getAttackingData(clientId));
			if (messager.getReceivedData().getTransmitType() == Transmit_Type_Game_End) {
				mainFrame.turnOnRound("You Win!");
				try {
					sleep(10000);
				} catch (InterruptedException e) {
					e.printStackTrace();
				}
				System.exit(0);
			} else if (messager.getReceivedData().getTransmitType() == Transmit_Type_Regular)
				opponentData = messager.getReceivedData();
			else if (messager.getReceivedData().getSize() > 0)
				receivedData = messager.getReceivedData();
			player.addCandy(1, control.hasReachedKing());
			player.addMoney();

		}
	}

	private void cushion() {
		final int CushionRound = 16;
		for (int i = 0; i < CushionRound; i++) {
			try {
				sleep(delay);
			} catch (InterruptedException e) {
				e.printStackTrace();
			}
			checkRunBridge();
			control.run();
			towerDefense_TransData = control.getInfo(clientId, timestamp);
			towerDefense_TransData.setTransmitType(Transmit_Type_Regular);
			messager.transmitRegularData(towerDefense_TransData);
			if (messager.getReceivedData().getTransmitType() == Transmit_Type_Game_End) {
				mainFrame.turnOnRound("You Win!");
				try {
					sleep(10000);
				} catch (InterruptedException e) {
					e.printStackTrace();
				}
				System.exit(0);
			} else if (messager.getReceivedData().getTransmitType() == Transmit_Type_Regular)
				opponentData = messager.getReceivedData();
			else if (messager.getReceivedData().getSize() > 0)
				receivedData = messager.getReceivedData();
			// paint
			if (draw_state == false)
				mainFrame.nextFrame(towerDefense_TransData);
			if (draw_state == true)
				mainFrame.nextFrame(opponentData);
		}
	}

	private void checkprepBridge() {
		client_bridge.setCandy(player.getCandy());
		client_bridge.setMoney(player.getMoney());
		// TODO Auto-generated method stub
		if (client_bridge.isCreateAttackUnitRequest()) {
			if (player.canCreateAttackingUnit(client_bridge.getAttackUnitId())) {
				player.createAttackingUnit(client_bridge.getAttackUnitId());
				client_bridge.setCreateAttackUnitRequest(false);
				client_bridge.setMoney(player.getMoney());
			}
		}
		if (client_bridge.isCreateUnitRequest()) {

			if (player.canCreateUnit(client_bridge.getId())) {
				System.out.println("Can Create ");
				if (control.addUnit(client_bridge.getId(),
						client_bridge.getX(), client_bridge.getY(), 1)) {

					player.createUnit(client_bridge.getId());
					client_bridge.setCandy(player.getCandy());
				}
			}
			client_bridge.setCreateUnitRequest(false);
		}

		if (client_bridge.isUnitLevelupRequest()) {
			 System.out.println(player.canUpdateUnit(client_bridge.getLevelupId()%100+10));
			 
			if (player.canUpdateUnit(client_bridge.getLevelupId() % 100 + 10)) {
				System.out.println("Can Create Unit");
				
				if (control.levelUp(client_bridge.getLevelupId())) {
					System.out.println("HAAAA");
					player.updateUnit(client_bridge.getLevelupId() % 100 + 10);
				}
				client_bridge.setCandy(player.getCandy());
				client_bridge.setUnitLevelupRequest(false);
			}
		}

		if (client_bridge.isCreateAttackUnitRequest()) {
			if (player.canCreateAttackingUnit(client_bridge.getAttackUnitId())) {
				player.createAttackingUnit(client_bridge.getAttackUnitId());
				client_bridge.setCreateAttackUnitRequest(false);
				client_bridge.setCandy(player.getCandy());
			}
		}
		if (client_bridge.isMeoMeoNumIncreaseRequest()) {
			if (player.canCreateMeoMeo()) {
				player.createMeoMeo();
				client_bridge.setMeoMeoNumIncreaseRequest(false);
				client_bridge.setCandy(player.getCandy());
				client_bridge.setCanCreateMeoMeo(true);

			}
		}
		if (client_bridge.isMeoMeoTechUpgradeRequest()) {
			if (player.canUpdateMeoMeo()) {
				player.updateMeoMeo();
				client_bridge.setMeoMeoNumIncreaseRequest(false);
				client_bridge.setCandy(player.getCandy());
				client_bridge.setCanUpgradeMeoMeo(true);
			}
		}
		if (client_bridge.isChangeViewRequest()) {
			draw_state = (!draw_state);
			client_bridge.setChangeViewRequest(false);
		}

	}

	public boolean checkInitalBridge() {

		if (client_bridge.isCreateGameRequest()) {

			client_bridge.setGameCreated(true);
			client_bridge.setCreateGameRequest(false);
			setClientId(Messager.Id_Server);
			start();
			return true;

		}
		if (client_bridge.isJoinGameRequest()) {
			try {
				sleep(1000);
			} catch (InterruptedException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			client_bridge.setGameConnected(true);
			setClientId(Messager.Id_Client);
			start();
			return true;

		}
		return false;
	}

	private void checkRunBridge() {
		client_bridge.setCandy(player.getCandy());
		client_bridge.setMoney(player.getMoney());
		// TODO Auto-generated method stub
		if (client_bridge.isCreateAttackUnitRequest()) {
			if (player.canCreateAttackingUnit(client_bridge.getAttackUnitId())) {
				player.createAttackingUnit(client_bridge.getAttackUnitId());
				client_bridge.setCreateAttackUnitRequest(false);
				client_bridge.setCandy(player.getCandy());
			}
		}
		if (client_bridge.isMeoMeoNumIncreaseRequest()) {
			if (player.canCreateMeoMeo()) {
				player.createMeoMeo();
				client_bridge.setMeoMeoNumIncreaseRequest(false);
				client_bridge.setCandy(player.getCandy());
				client_bridge.setCanCreateMeoMeo(true);

			}
		}
		if (client_bridge.isMeoMeoTechUpgradeRequest()) {
			if (player.canUpdateMeoMeo()) {
				player.updateMeoMeo();
				client_bridge.setMeoMeoNumIncreaseRequest(false);
				client_bridge.setCandy(player.getCandy());
				client_bridge.setCanUpgradeMeoMeo(true);
			}
		}
		if (client_bridge.isChangeViewRequest()) {
			draw_state = (!draw_state);
			client_bridge.setChangeViewRequest(false);
		}

	}
}
