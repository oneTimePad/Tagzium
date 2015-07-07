#CHESS AI with Alpha-Beta pruning
#Needs commenting








import sys
import random
static_values={"king":1,"queen":9,"rook":4,"knight":3,"bishop":3,"pawn":1}


class Chess_Game(object):

	def __init__(self):
		self.status=None
		self.black = []
		self.black_player = None
		self.white =[]
		self.white_player = None
		self.turn = "white"
		self.turns = 0
		self.white_king =None
		self.black_king = None
		self.board=self.generate_board()
		
		

	def get_other(self,player_name):
		if player_name is "white":
			return self.black_player
		else:
			return self.white_player

	def generate_legal(self,player):

		pieces = player.pieces

		legal_moves = []
		for piece in pieces:
			if piece.status is "alive":
				for move in piece.legal():
					legal_moves.append((piece,move))

		king = None
		if player.color is "white":
			
			king = game.white_king
		else:
			
			king = game.black_king 

		legs = []
		for moves in legal_moves:
			piece,move = moves
			self.make_move(piece,move)
			if king.get_attacking() is 0:
				legs.append((piece,move))
			self.unmake_move(piece)


		return legs






	def make_move(self,piece,move):
		

		if move.piece is not None:
			move.piece.status = "dead"
		piece.prev_states.append(self.status)
		piece.last_positions.append(piece.position)
		piece.position.piece = None
		if move.piece is not None:
			piece.prev_kills.append(move.piece)
		else:
			piece.prev_kills.append(None)
		move.piece = piece
		piece.position = move

	def unmake_move(self,piece):
		prev_pos = piece.last_positions.pop()
		kill = piece.prev_kills.pop()
		if kill:
			kill.status = "alive"
			piece.position.piece = kill
		else:
			piece.position.piece = None
		piece.position = prev_pos
		prev_pos.piece = piece





	def heuristic(self,player):
		pieces = player.pieces
		total = 0
		for piece in pieces:
			if piece.status is "dead":
				continue
			attack = piece.attack()

			attacking = piece.get_attacking()
			val = piece.value
			
			

			sum = attack*val-attacking*val
			total = total + sum
		
		return total

	def generate_board(self):
		board = []
		for i in xrange(0,8):
			board.append([])
		for i in xrange(0,8):
			for j in xrange(0,8):
				board[i].append(Position(None,(i,j),board))

		for i in xrange(0,8):
			for j in xrange(0,2):
				if j is 1:
					board[i][j].piece=Pawn("pawn","b",(i,j),self)
					board[i][j].piece.position=board[i][j]
					self.black.append(board[i][j].piece)
				if i is 0 and j is 0 or i is 7 and j is 0:
					board[i][j].piece=Rook("rook","b",(i,j),self)
					board[i][j].piece.position=board[i][j]
					self.black.append(board[i][j].piece)
				if i is 1 and j is 0 or i is 6 and j is 0:
					board[i][j].piece=Knight("knight","b",(i,j),self)
					board[i][j].piece.position=board[i][j]
					self.black.append(board[i][j].piece)
				if i is 2 and j is 0 or i is 5 and j is 0:
					board[i][j].piece=Bishop("bishop","b",(i,j),self)
					board[i][j].piece.position=board[i][j]
					self.black.append(board[i][j].piece)
				if i is 3 and j is 0:
					board[i][j].piece=King("king","b",(i,j),self)
					board[i][j].piece.position=board[i][j]
					self.black.append(board[i][j].piece)
					self.black_king = board[i][j].piece
					
				if i is 4 and j is 0:
					board[i][j].piece=Queen("queen","b",(i,j),self)
					board[i][j].piece.position=board[i][j]
					self.black.append(board[i][j].piece)

		for i in xrange(0,8):
			for j in xrange(6,8):
				if j is 6:
					board[i][j].piece=Pawn("pawn","w",(i,j),self)
					board[i][j].piece.position=board[i][j]
					self.white.append(board[i][j].piece)
				if i is 0 and j is 7 or i is 7 and j is 7:
					board[i][j].piece=Rook("rook","w",(i,j),self)
					board[i][j].piece.position=board[i][j]
					self.white.append(board[i][j].piece)
				if i is 1 and j is 7 or i is 6 and j is 7:
					board[i][j].piece=Knight("knight","w",(i,j),self)
					board[i][j].piece.position=board[i][j]
					self.white.append(board[i][j].piece)
				if i is 2 and j is 7 or i is 5 and j is 7:
					board[i][j].piece=Bishop("bishop","w",(i,j),self)
					board[i][j].piece.position=board[i][j]
					self.white.append(board[i][j].piece)
				if i is 3 and j is 7:
					board[i][j].piece=King("king","w",(i,j),self)
					board[i][j].piece.position=board[i][j]
					self.white.append(board[i][j].piece)
					self.white_king = board[i][j].piece
					
				if i is 4 and j is 7:
					board[i][j].piece=Queen("queen","w",(i,j),self)
					board[i][j].piece.position=board[i][j]
					self.white.append(board[i][j].piece)
		return board

class Mind(object):

	def __init__(self):
		pass

	def alphabeta(self,game,alpha,beta,ply,player,playing):

		if ply ==0:
			return game.heuristic(player)

		move_list = game.generate_legal(player)
		
		if len(move_list) == 0 and player.color is not playing:
			return 10000000
		if len(move_list) == 0 and player.color is playing:
			return -1000000 

		for move in move_list:
			piece, move =move

			game.make_move(piece,move)
			


			current_eval = -self.alphabeta(game,-beta,-alpha,ply-1,game.get_other(player.color),playing)

			game.unmake_move(piece)

			if current_eval >= beta:
				return beta

			if current_eval > alpha:
				alpha = current_eval
		return alpha

	def rootAlphaBeta(self,game,ply,player,playing):

		best_move = None
		max_eval = float('-infinity')

		move_list = game.generate_legal(player)

		for move in move_list:
			piece, move = move
			game.make_move(piece,move)
			current_eval = -self.alphabeta(game,float('-infinity'),float('infinity'),ply-1,game.get_other(player),playing)
			game.unmake_move(piece)

			if current_eval > max_eval:
				max_eval = current_eval
				best_move = (piece,move)

		return best_move














def best_move(game,player):



	print "Thinking...please wait"
	mover = Mind()

	#If you want to make it look further into the future
	#change the two parameter to a higher number
	#beware, higher numbers result in higher computation time
	piece,move= mover.rootAlphaBeta(game,2,player,player.color)

	return piece,move



class Player(object):

	def __init__(self,color,pieces,game):
		self.color = color
		self.pieces = pieces
		self.letters= ['A','B','C','D','E','F','G','H']
		self.game=game

	def move_player(self,froms,move):
		board = self.game.board

		
		x,y = froms
		piece=board[x][y].piece
		board[x][y].piece = None
		x,y = move
		old_piece = board[x][y].piece
		if old_piece is not None:
			if old_piece.color is not piece.color:
				old_piece.status ="dead"
		board[x][y].piece = piece
		piece.position=board[x][y]

		

	def move_ai(self):

		legal_moves =[]
		piece = None
		move = None
		if self.game.turns < 3:

			legal_moves = self.game.generate_legal(self)
			
			integer = random.randint(0,len(legal_moves)-1)

			piece,move =legal_moves[integer]
		else:
			piece,move = best_move(self.game,self)
		
		x,y = piece.position.location
		w,z = move.location
		print "Move ", piece.piece, " from", self.letters[x],y, " to ", self.letters[w],z 
		self.game.make_move(piece,move)

		print "Status", self.game.status




class Position(object):

	def __init__(self,piece,location,board):
		self.piece = piece
		self.location = location
		self.board = board

	def in_range(self,x,y):
		if x < 0 or x >7:
			return False
		if y < 0 or y > 7:
			return False
		return True

	def north(self):
		x,y = self.location
		y-=1

		if self.in_range(x,y):
			return self.board[x][y]
		return -1
	def south(self):
		x,y = self.location
		y+=1

		if self.in_range(x,y):
			return self.board[x][y]
		return -1
	def north_east(self):
		x,y = self.location
		x+=1
		y-=1

		if self.in_range(x,y):
			return self.board[x][y]
		return -1

	def north_west(self):
		x,y = self.location
		x-=1
		y-=1
		if self.in_range(x,y):
			return self.board[x][y]
		return -1
	def south_east(self):
		x,y = self.location
		x+=1
		y+=1
		if self.in_range(x,y):
			return self.board[x][y]
		return -1

	def south_west(self):
		x,y = self.location
		x-=1
		y+=1
		if self.in_range(x,y):
			return self.board[x][y]
		return -1

	def east(self):
		x,y = self.location
		x+=1
		if self.in_range(x,y):
			return self.board[x][y]
		return -1

	def west(self):
		x,y = self.location
		x-=1
		if self.in_range(x,y):
			return self.board[x][y]
		return -1




class Piece(object):

	def __init__(self,piece,color,position,game):
		self.color=color
		self.piece= piece
		self.value=static_values[piece]
		self.position = position
		self.last_positions =[]
		self.game = game
		self.prev_states = []
		self.prev_kills = []
		self.status = "alive"
	
	def get_legal(self):
		pass
	
	def legal(self):
		legals = self.get_legal()
		
		leg =[]
		for pos in legals:
			if pos.piece is not None:
				if pos.piece.color is not self.color:
					leg.append(pos)
			else:
				leg.append(pos)
		return leg


	def move(self,move):
		self.last_position = self.position
		move.piece = self
		self.position = move



	def can_kill(self,piece):
		
		if self.piece is "king" and piece.piece is "king":
			return False

		legals=piece.legal()
		
		for move in legals:
			if move.piece is not None:
				if move.piece is self:
					return True
		return False



	def get_attacking(self):
		pos = self.position


		pieces = []


		while True:
			pos = pos.north()

			if pos is -1:
				break

			if pos.piece is not None:
				if pos.piece.color is not self.color:
					
					if self.can_kill(pos.piece):
						pieces.append(pos.piece)
				break

		pos = self.position

		while True:
			pos = pos.south()
			if pos is -1:
				break
			if pos.piece is not None:
				if pos.piece.color is not self.color:
					
					if self.can_kill(pos.piece):
						pieces.append(pos.piece)
				break

		pos = self.position

		while True:
			pos = pos.east()
			if pos is -1:
				break
			if pos.piece is not None:
				if pos.piece.color is not self.color:
					
					if self.can_kill(pos.piece):
						pieces.append(pos.piece)
				break


		pos = self.position

		while True:
			pos = pos.west()
			if pos is -1:
				break
			if pos.piece is not None:
				if pos.piece.color is not self.color:
					
					if self.can_kill(pos.piece):
						pieces.append(pos.piece)
				break


		pos = self.position

		while True:
			pos = pos.north_east()
			if pos is -1:
				break
			if pos.piece is not None:
				if pos.piece.color is not self.color:
					
					if self.can_kill(pos.piece):
						pieces.append(pos.piece)
				break

		pos = self.position

		while True:
			pos = pos.north_west()
			if pos is -1:
				break
			if pos.piece is not None:
				if pos.piece.color is not self.color:
					
					if self.can_kill(pos.piece):
						pieces.append(pos.piece)
				break

		pos = self.position

		while True:
			pos = pos.south_east()
			if pos is -1:
				break
			if pos.piece is not None:
				if pos.piece.color is not self.color:
					
					if self.can_kill(pos.piece):
						pieces.append(pos.piece)
				break

		pos = self.position

		while True:
			pos = pos.south_west()
			if pos is -1:
				break
			if pos.piece is not None:
				if pos.piece.color is not self.color:
					
					if self.can_kill(pos.piece):
						pieces.append(pos.piece)
				break

		if len(pieces) is not 0:
			maxi= pieces[0]
			for piece in pieces:
				if piece.value > maxi.value:
					maxi = piece
			if self.piece is "king":
				self.game.status = "check"
				#possible change
				return 1000000
			return maxi.value
		else:
			return 0


	def attack(self):
		pass

class King(Piece):

	def get_legal(self):
		legal_positions=[self.position.north(),self.position.south(),self.position.north_east(),self.position.north_west(),self.position.south_west(),self.position.south_east(),self.position.east(),self.position.west()]
		leg = []
		legs = []
		for pos in legal_positions:
			if pos is not -1:
				leg.append(pos)
		for pos in leg:
			if pos.piece is not None:
				if pos.piece.color is not self.color:
					legs.append(pos)
			else:
				legs.append(pos)
		if len(legs) is 0:
			return []


		legal = []
		stor = self.position
		status = self.game.status
		for pos in legs:
				self.position = pos
				self.get_attacking()
				if self.get_attacking() is 0:
					legal.append(pos)
				self.position = stor
				

		return legal


	def attack(self):
		legal_positions=[self.position.north(),self.position.south(),self.position.north_east(),self.position.north_west(),self.position.south_west(),self.position.south_east(),self.position.east(),self.position.west()]
		legal = []
		legs = []
		for pos in legal_positions:
			if pos is not -1:
				legal.append(pos)

		for pos in legal:
			if pos.piece is not None:
				if pos.piece.color is not self.color:
					legs.append(pos)
			else:
				legs.append(pos)
		if len(legs) is 0:
			return 0


		count = 0

		for pos in legal:
			if pos.piece is not None and pos.piece.color is not self.color:
				count+=1
		
		return count



class Queen(Piece):

	def get_legal(self):
		legal_moves = []

		move = self.position


		while True:
			move = move.north()
			if move is -1:
				break
			if move.piece is not None:
				if move.piece.color is self.color:
					break
			legal_moves.append(move)
		move = self.position
		while True:
			move = move.south()
			if move is -1:
				break
			if move.piece is not None:
				if move.piece.color is self.color:
					break
			legal_moves.append(move)
		move = self.position
		while True:
			move = move.north_west()
			if move is -1:
				break
			if move.piece is not None:
				if move.piece.color is self.color:
					break
			legal_moves.append(move)
		move = self.position
		while True:
			move = move.north_east()
			if move is -1:
				break
			if move.piece is not None:
				if move.piece.color is self.color:
					break
			legal_moves.append(move)
		move = self.position
		while True:
			move = move.south_east()
			if move is -1:
				break
			if move.piece is not None:
				if move.piece.color is self.color:
					break
			legal_moves.append(move)
		move = self.position
		while True:
			move = move.south_west()
			if move is -1:
				break
			if move.piece is not None:
				if move.piece.color is self.color:
					break
			legal_moves.append(move)
		move = self.position
		while True:
			move = move.east()
			if move is -1:
				break
			if move.piece is not None:
				if move.piece.color is self.color:
					break
			legal_moves.append(move)
		move = self.position
		while True:
			move = move.west()
			if move is -1:
				break
			if move.piece is not None:
				if move.piece.color is self.color:
					break
			legal_moves.append(move)



		return legal_moves

	def attack(self):
		count = 0 
		move =self.position
		while True:
			move = move.north()
			if move is -1:
				break
			if move.piece is not None and move.piece.color is self.color:
				break
			if move.piece is not None and move.piece.color is not self.color:
				count +=1
			
		move = self.position
		while True:
			move = move.south()

			if move is -1:
				break
			if move.piece is not None and move.piece.color is self.color:
				break
			if move.piece is not None and move.piece.color is not self.color:
				count +=1
		move = self.position
		while True:
			move = move.north_west()
			if move is -1:
				break
			if move.piece is not None and move.piece.color is self.color:
				break
			if move.piece is not None and move.piece.color is not self.color:
				count +=1
		move = self.position
		while True:
			move = move.north_east()
			if move is -1:
				break
			if move.piece is not None and move.piece.color is self.color:
				break
			if move.piece is not None and move.piece.color is not self.color:
				count +=1
		move = self.position
		while True:
			move = move.south_east()
			if move is -1:
				break
			if move.piece is not None and move.piece.color is self.color:
				break
			if move.piece is not None and move.piece.color is not self.color:
				count +=1
		move = self.position
		while True:
			move = move.south_west()
			if move is -1:
				break
			if move.piece is not None and move.piece.color is self.color:
				break
			if move.piece is not None and move.piece.color is not self.color:
				count +=1
		move = self.position
		while True:
			move = move.east()
			if move is -1:
				break
			if move.piece is not None and move.piece.color is self.color:
				break
			if move.piece is not None and move.piece.color is not self.color:
				count +=1
		move = self.position
		while True:
			move = move.west()
			if move is -1:
				break
			if move.piece is not None and move.piece.color is self.color:
				break
			if move.piece is not None and move.piece.color is not self.color:
				count +=1



		return count

class Rook(Piece):

	def get_legal(self):

		legal_moves = []

		move = self.position

		while True:
			move = move.north()
			if move is -1:
				break
			if move.piece is not None and move.piece.color is self.color:
				break
			legal_moves.append(move)
		move = self.position
		while True:
			move = move.south()
			if move is -1:
				break
			if move.piece is not None and move.piece.color is self.color:
				break
			legal_moves.append(move)
		move = self.position
		while True:
			move = move.east()
			if move is -1:
				break
			if move.piece is not None and move.piece.color is self.color:
				break
			legal_moves.append(move)
		move = self.position
		while True:
			move = move.west()
			if move is -1:
				break
			if move.piece is not None and move.piece.color is self.color:
				break
			legal_moves.append(move)


	
		return legal_moves

	def attack(self):
		count =0
		move = self.position

		while True:
			move = move.north()

			

			if move is -1:
				break


			if (move.piece is not None) and (move.piece.color is self.color):
				
				break
			if move.piece is not None and move.piece.color is not self.color:
				count +=1
		move = self.position
		while True:
			move = move.south()

			if move is -1:

				break
			if move.piece is not None and move.piece.color is self.color:
				
				break
			if move.piece is not None and move.piece.color is not self.color:
				count +=1
		move = self.position
		while True:
			move = move.east()

			if move is -1:
				break
			if move.piece is not None and move.piece.color is self.color:
				
				break
			if move.piece is not None and move.piece.color is not self.color:
				count +=1
		move = self.position
		while True:
			move = move.west()

			if move is -1:
				break
			if move.piece is not None and move.piece.color is self.color:
				
				break
			if move.piece is not None and move.piece.color is not self.color:
				count +=1

		
		return count


class Bishop(Piece):

	def get_legal(self):
		legal_moves = []

		move = self.position

		while True:
			move = move.north_west()
			if move is -1:
				break
			if move.piece is not None and move.piece.color is self.color:
				break
			legal_moves.append(move)
		move = self.position	
		while True:
			move = move.north_east()
			if move is -1:
				break
			if move.piece is not None and move.piece.color is self.color:
				break
			legal_moves.append(move)
		move = self.position
		while True:
			move = move.south_east()
			if move is -1:
				break
			if move.piece is not None and move.piece.color is self.color:
				break
			legal_moves.append(move)
		move = self.position
		while True:
			move = move.south_west()
			if move is -1:
				break
			if move.piece is not None and move.piece.color is self.color:
				break
			legal_moves.append(move)

		return legal_moves

	def attack(self):
		count = 0
		move = self.position

		while True:
			move = move.north_west()
			if move is -1:
				break
			if move.piece is not None and move.piece.color is self.color:
				break
			if move.piece is not None and move.piece.color is not self.color:
				count +=1
		move = self.position
		while True:
			move = move.north_east()
			if move is -1:
				break
			
			if move.piece is not None and move.piece.color is self.color:
				break
			if move.piece is not None and move.piece.color is not self.color:
				count +=1
		move = self.position
		while True:
			move = move.south_east()
			if move is -1:
				break
			
			if move.piece is not None and move.piece.color is self.color:
				break
			if move.piece is not None and move.piece.color is not self.color:
				count +=1
		move = self.position
		while True:
			move = move.south_west()
			if move is -1:
				break
			
			if move.piece is not None and move.piece.color is self.color:
				break
			if move.piece is not None and move.piece.color is not self.color:
				count +=1

		return count




class Pawn(Piece):

	def get_legal(self):
		
		legal_positions = []
		
		


		if self.color is "b":
			if self.position.south() is not -1:
				
				if self.position.south().piece is None:
					
					legal_positions.append(self.position.south())

			pos = self.position
			x,y = pos.location

			if x == 1:
				if self.position.south() is not -1:
					if self.position.south().south() is not -1:
						if self.position.south().south().piece is None:

							legal_positions.append(self.position.south().south())

			if self.position.south_west() is not -1:
				if self.position.south_west().piece is not None:
					legal_positions.append(self.position.south_west())
			if self.position.south_east() is not -1:
				if self.position.south_east().piece is not None:
					legal_positions.append(self.position.south_east())

		if self.color is "w":
			if self.position.north() is not -1:
				
				if self.position.north().piece is None:
					
					legal_positions.append(self.position.north())
			pos = self.position
			x,y=pos.location
			legal_positions.append(self.position.north())
			if x == 6:
				if self.position.north() is not -1:
					if self.position.north().north() is not -1:
						if self.position.north().north().piece is None:
							legal_positions.append(self.position.north().north())


			if self.position.north_west() is not -1:
				if self.position.north_west().piece is not None:
					legal_positions.append(self.position.north_west())
			if self.position.north_east() is not -1:
				if self.position.north_east().piece is not None:
					legal_positions.append(self.position.north_east())
		
		return legal_positions

	def attack(self):
		count = 0
		if self.color is "Black":
			

			if self.position.south_west().piece is not None and self.position.south_west().piece.color is not self.color:
				count +=1
			if self.position.south_east().piece is not None and self.position.south_east().piece.color is not self.color:
				count +=1

		if self.color is "White":
			legal_positions.append(self.position.north())

			if self.position.north_west.piece() is not None and self.position.north_west().piece.color is not self.color:
				count +=1
			if self.position.north_east.piece() is not None and self.position.north_east().piece.color is not self.color:
				count +=1
		return count
	


class Knight(Piece):

	def get_legal(self):

		pos = self.position
		legal_moves = []

		if pos.north() is not -1:
			if pos.north().north() is not -1:
				if pos.north().north().east() is not -1:
					if pos.north().north().east() is not None and pos.north().north().east().piece is not self.color:
						legal_moves.append(pos.north().north().east())
					else:
						legal_moves.append(pos.north().north().east())

		pos = self.position
		if pos.north() is not -1:
			if pos.north().north() is not -1:
				if pos.north().north().west() is not -1:
					if pos.north().north().west() is not None and pos.north().north().west().piece is not self.color:
						legal_moves.append(pos.north().north().west())
					else:
						legal_moves.append(pos.north().north().west())
		pos = self.position
		if pos.south() is not -1:
			if pos.south().south() is not -1:
				if pos.south().south().west() is not -1:
					if pos.south().south().west() is not None and pos.south().south().west().piece is not self.color:
						legal_moves.append(pos.south().south().west())
					else:
						legal_moves.append(pos.south().south().west())

		pos = self.position
		if pos.south() is not -1:
			if pos.south().south() is not -1:
				if pos.south().south().east() is not -1:
					if pos.south().south().east() is not None and pos.south().south().east().piece is not self.color:
						legal_moves.append(pos.south().south().east())
					else:
						legal_moves.append(pos.south().south().east())
		pos = self.position
		if pos.east() is not -1:
			if pos.east().east() is not -1:
				if pos.east().east().north() is not -1:
					if pos.east().east().north() is not None and pos.east().east().north().piece is not self.color:
						legal_moves.append(pos.east().east().north())
					else:
						legal_moves.append(pos.east().east().north())
		pos = self.position
		if pos.east() is not -1:
			if pos.east().east() is not -1:
				if pos.east().east().south() is not -1:
					if pos.east().east().south() is not None and pos.east().east().south().piece is not self.color:
						legal_moves.append(pos.east().east().south())
					else:
						legal_moves.append(pos.east().east().south())
		pos = self.position

		if pos.west() is not -1:
			if pos.west().west() is not -1:
				if pos.west().west().north() is not -1:
					if pos.west().west().north() is not None and pos.west().west().north().piece is not self.color:
						legal_moves.append(pos.west().west().north())
					else:
						legal_moves.append(pos.west().west().north())
		pos = self.position
		if pos.west() is not -1:
			if pos.west().west() is not -1:
				if pos.west().west().south() is not -1:
					if pos.west().west().south() is not None and pos.west().west().south().piece is not self.color:
						legal_moves.append(pos.west().west().south())
					else:
						legal_moves.append(pos.west().west().south())

		return legal_moves

	def attack(self):
		pos = self.position
		count = 0
		if pos.north() is not -1:
			if pos.north().north() is not -1:
				if pos.north().north().east() is not -1:
					if pos.north().north().east().piece is not None and pos.north().north().east().piece.color is not self.color:
						count +=1
		pos = self.position
		if pos.north() is not -1:
			if pos.north().north() is not -1:
				if pos.north().north().west() is not -1:
					if pos.north().north().west().piece is not None and pos.north().north().west().piece.color is not self.color:
						count +=1
		pos = self.position
		if pos.south() is not -1:
			if pos.south().south() is not -1:
				if pos.south().south().west() is not -1:
					if pos.south().south().west().piece is not None and pos.south().south().west().piece.color is not self.color:
						count +=1
		pos = self.position
		if pos.south() is not -1:
			if pos.south().south() is not -1:
				if pos.south().south().east() is not -1:
					if pos.south().south().east().piece is not None and pos.south().south().east().piece.color is not self.color:
						count +=1
		pos = self.position
		if pos.east() is not -1:
			if pos.east().east() is not -1:
				if pos.east().east().north() is not -1:
					if pos.east().east().north().piece is not None and pos.east().east().north().piece.color is not self.color:
						count +=1
		pos = self.position
		if pos.east() is not -1:
			if pos.east().east() is not -1:
				if pos.east().east().south() is not -1:
					if pos.east().east().south().piece is not None and pos.east().east().south().piece.color is not self.color:
						count +=1

		pos = self.position
		if pos.west() is not -1:
			if pos.west().west() is not -1:
				if pos.west().west().north() is not -1:
					if pos.west().west().north().piece is not None and pos.west().west().north().piece.color is not self.color:
						count +=1
		pos = self.position
		if pos.west() is not -1:
			if pos.west().west() is not -1:
				if pos.west().west().south() is not -1:
					if pos.west().west().south().piece is not None and pos.west().west().south().piece.color is not self.color:
						count +=1

		return count


def print_board(game):
	for i in xrange(0,2):
		for j in xrange(0,8):
			if game.board[j][i].piece is not None:
				print game.board[j][i].piece.piece,"(",game.board[j][i].piece.color,")",
			else:
				print game.board[j][i].piece,
		print '\n'
	for i in xrange(2,6):
		for j in xrange(0,8):
			if game.board[j][i].piece is not None:
				print game.board[j][i].piece.piece,"(",game.board[j][i].piece.color,")",
			else:
				print game.board[j][i].piece,
		print '\n'

	for i in xrange(6,8):
		for j in xrange(0,8):
			if game.board[j][i].piece is not None:
				print game.board[j][i].piece.piece,"(",game.board[j][i].piece.color,")",
			else:
				print game.board[j][i].piece,
		print '\n'



if __name__ == "__main__":

	game = Chess_Game()

	num = random.randint(0,10)
	
	AI = None
	player = None
	if num >= 5:
		AI = Player("white",game.white,game)
		game.white_player = AI
		player = Player("black",game.black,game)
		game.black_player = player
	else:
		AI = Player("black",game.black,game)
		game.black_player = AI
		player = Player("white",game.white,game)
		game.white_player = player

	print "\n"
	print "AI is ", AI.color
	print "You are ", player.color+"\n"

	print "Input move when asked, example input: A,1,A,2\n"



 	lookup={"A":0,"B":1,"C":2,"D":3,"E":4,"F":5,"G":6,"H":7}


	while game.status != "Check-mate":

		turn = game.turn
		if AI.color == turn:
			print AI.color, "turn\n"
			AI.move_ai()
		else:
			print player.color, " turn\n"
			move = raw_input("Your turn")
			
			print move
			#sample_move = "B,2,B,3"
			pos = move.split(",")
			
			pos[1]=int(pos[1])
			pos[3]=int(pos[3])
			pos[1]-=1
			pos[3]-=1
			pos[0]=lookup[pos[0]]
			pos[2]=lookup[pos[2]]

			player.move_player((int(pos[0]),pos[1]),(int(pos[2]),pos[3]))
			
		

		king = None
		print game.turn

		if game.turn is "white":
			
			king = game.black_king
		else:
			king = game.white_king
			
		if king.get_attacking() is not 0:
			game.status = "check"
			
		else:
			game.status = "none"

		if game.turn is"white":
			game.turn = "black"
		else:
			game.turn = "white"

		if game.status is "check":
			print "player ", game.turn," is in check"


		game.turns+=1

		print_board(game)

		
	print "Check-mate ","Total turns: ", game.turns
	


		

				
