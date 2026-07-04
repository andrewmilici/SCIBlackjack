;;; Sierra Script 1.0 - (do not remove this comment)
;
; SCI Template Game
; By Brian Provinciano
; ******************************************************************************
; rm001.sc
; Contains the first room of your game. 
(script# 1)
(include sci.sh)
(include game.sh)
(use main)
(use controls)
(use cycle)
(use game)
(use feature)
(use obj)
(use inv)
(use door)
(use jump)
(use dpath)

(public
	rm001 0
)

(local
	gameState
	dealerText
	egoText	
	dealerTotal 
	egoTotal
	hiddenDealerCard
)

(procedure (EgoDead)
	(gGame setScript: (ScriptID DYING_SCRIPT)) ; DyingScript
	((ScriptID DYING_SCRIPT) ; DyingScript
		caller: (+ 4 (gEgo view:))
		register: {You are out of cash!} ; "Next time, %s, stay out of that polluted lagoon!"
		next:  {text 2} ; "You're in over your head again"
	)
	
)

(procedure (CheckPlayerBalance)
 	(if (> gScore 0)
 		(return)
	)
	
	(EgoDead)
	
)

(procedure (ChangeBet &tmp temp0 temp1)
	(if (> gameState 0)
		(Print 11 8)
	else	
		(= temp1 0)
		
		(while (== temp1 0)
			(= temp0 (GetNumber {Enter new bet:}))
				
			(if (> temp0 gScore)
				(Print 11 9)	
			else
				(if (<= temp0 0)
					(Print 11 10)	
				else
					(if (!= (mod temp0 2) 0)
						(Print 11 11)
					else	
						(= temp1 1)		
					)
				

				)
			)
		)
		
		(= gCurrentBet temp0)
		(SL doit:)	
	)	
	
)
(procedure (GetNextCard &tmp num)
	(if gDebugging
		(return (= num (GetNumber {card #})))
	else
		(= num (Random 0 51))
		(if (== (deck at: num) -1)
			(= num (GetNextCard))
		else
			(deck delete: (deck at: num))
			(deck addAfter: (- num 1) -1)
			(return num)
		)
	)
)

(procedure (WinHand)
	(AddScore (* gCurrentBet 2))
	(rm001 setScript: RoomScript)
	(dealingScript dispose:)
	(standScript dispose:)
	(HandsOn)
)

(procedure (CheckResult)

	(if (== gameState gStatePlayerTurn)
		(if (> egoTotal 21)
			(Print 11 2)
			(Print 11 3 #at -1 144)
			(= gameState gStateNotStarted)
			(CheckPlayerBalance)
		)
	)
	(if (== gameState gStateCPUTurn)
		(if (> dealerTotal 21)
			(Print 11 4)
				(youWinAnim
					setCycle: Fwd  
					setScript: youWinScript
					init:)	
			(= gameState gStateNotStarted)

			(WinHand)
		)
		
	)
	
)

(procedure (CheckFinalResult)
	(HandsOn)
	(if (== egoTotal dealerTotal)
		(Print 11 5)	
		(+= gScore gCurrentBet) ;manually doing it to avoid triggering the win sound
		(SL doit:)
		(rm001 setScript: RoomScript)
		(dealingScript dispose:)
		(standScript dispose:)
		(= gameState gStateNotStarted)
		(return)	
	)	
	
	(if (< egoTotal dealerTotal)
		(Print 11 6)
		(rm001 setScript: RoomScript)
		(dealingScript dispose:)
		(standScript dispose:)	
		(= gameState gStateNotStarted)
		(CheckPlayerBalance)
		(return)	
	)
	
	(if (> egoTotal dealerTotal)
		(youWinAnim
			setCycle: Fwd  
			setScript: youWinScript
			init:)
		(WinHand)
		(return)
	)
	
)

;(procedure (

(procedure (RecalcTotals &tmp i node calc dealerAces egoAces [temp4 400])
	;(Display 11 13 dsCOORD 10 15 dsRESTOREPIXELS dealerText)
	;(Display 11 13 dsCOORD 10 115 dsRESTOREPIXELS egoText)
	(= dealerTotal 0)
	(= egoTotal 0)
	(= dealerAces 0)
	(= egoAces 0)
	
	(for ((= i 0)) (< i (dealerCards size:)) ((++ i))
		(= node (dealerCards at: i))
		(= calc node)
		
		(if (> calc 10)
			(= calc 10)	
		)
		
		(if (== calc 1) ; Ace - count as 11 for now, track how many we have
			
				(= calc 11)
				(++ dealerAces)
			
		)
		
		(+= dealerTotal calc)
	)
	
	; If we're over 21 and have aces, convert them from 11 to 1 (subtract 10) one at a time
	(while (and (> dealerTotal 21) (> dealerAces 0))
		(-= dealerTotal 10)
		(-- dealerAces)
	)
	
	(Format @temp4 {%d} dealerTotal)
	;(= dealerText (Display @temp4 dsCOORD 10 15 dsWIDTH 100 dsCOLOR 15 dsSAVEPIXELS))
	
	(for ((= i 0)) (< i (egoCards size:)) ((++ i))
		(= node (egoCards at: i))
		(= calc node)
		
		(if (> calc 10)
			(= calc 10)	
		)
		
		(if (== calc 1) ; Ace - count as 11 for now, track how many we have
				(= calc 11)
				(++ egoAces)
			
		)
		
		(+= egoTotal calc)
	)
	
	; If we're over 21 and have aces, convert them from 11 to 1 (subtract 10) one at a time
	(while (and (> egoTotal 21) (> egoAces 0))
		(-= egoTotal 10)
		(-- egoAces)
	)
	
	(Format @temp4 {%d} egoTotal)
	;(= egoText (Display @temp4 dsCOORD 10 115 dsWIDTH 100 dsCOLOR 15 dsSAVEPIXELS))
)


(procedure (AddNewCard cardId isEgo &tmp newCard view loop cel newX cardCount cardValue)
	(= newCard (Clone card))
	(= view (/ cardId 13))   ; 0-12 = spades, 13-25 = clubs, etc.
    (= loop 0)
    (= cel (+ (mod cardId 13) 1))  ; 0-12 mod 13 + 1 = cels 1-13
    (= cardValue cel)
	(newCard 
		view: view
		loop: loop
		cel: cel)
		;x 60
	(if (== isEgo 1)
		(newCard y: 130)
		(= cardCount (egoCards size:))
	else
		(newCard y: 25)	
		(= cardCount (dealerCards size:))
		
		(if (== cardCount 1)
			(= hiddenDealerCard (Clone newCard))
			
			(hiddenDealerCard x: 75)
			(screenCards addToEnd: hiddenDealerCard)
			(newCard 
				view: 200
				loop: 0
				cel: 0)	
		)
	)
	(= newX (+ (* 40 cardCount) 35))
	
	;60 + (40 * cardcount)
	
	(newCard
		x: newX 
		init:)
		
	(screenCards addToEnd: newCard)
		
	(if (== isEgo 1)
		(egoCards addToEnd: cardValue)
	else
		(dealerCards addToEnd: cardValue)
	)

	
)

(procedure (NewGame &tmp i node)
	; Check if we have enough cash to proceed?
	(if (> gCurrentBet gScore)
		(Print 11 1)
		(return)
	)
	
	; Clear screen (cards, totals etc)
	(dealerCards release:)
	(egoCards release:)

	;(Display 11 13 dsCOORD 10 15 dsRESTOREPIXELS dealerText)
	;(Display 11 13 dsCOORD 10 115 dsRESTOREPIXELS egoText)
	
	(for ((= i 0)) (< i (screenCards size:)) ((++ i))
		(= node (screenCards at: i))
		(node dispose:)
	)
	(screenCards release:)
	
	;Set game state to "dealing"
	(= gameState gStateDealing)
	;Deduct bet
	(-= gScore gCurrentBet)
	(SL doit:)	
	
	;Shuffle the deck
	(ShuffleDeck)
	
	;Start Shuffle Animation
	(shuffleAnim 
		setCycle: Fwd  
		setScript: shuffleScript
		init:)	
)

(procedure (ShuffleDeck &tmp i)
	(deck release:)
	
	(for ((= i 0)) (< i 52) ((++ i))
		(deck addToEnd: i)
	)
	
)

(instance youWinAnim of Prop
	(properties
		view 5
		loop 0
		x 150
		y 205
	)
)

(instance youWinScript of Script
	(properties)

	(method (changeState newState &tmp newcard)
		(switch (= state newState)
			(0
				(= seconds 3)
			)
			(1
				(youWinAnim dispose:)
				(youWinScript dispose:)
				;(rm001 setScript: RoomScript)		
				(= gameState gStateNotStarted)		
			)	
		)
	)
)

(instance shuffleAnim of Prop
	(properties
		view 11
		loop 0
		x 150
		y 40
	)
)

(instance card of View
	(properties)
)

(instance shuffleScript of Script
	(properties)

	(method (changeState newState)
		(switch (= state newState)
			(0
				(= cycles 15)
			)
			(1
				(shuffleAnim dispose:)
				(shuffleScript dispose:)
				(rm001 setScript: dealingScript)
				
			)
		)
	)
)

(instance deck of List
	(properties)	
)

(instance egoCards of List
	(properties)	
)

(instance dealerCards of List
	(properties)
	
)
(instance screenCards of List
	(properties)
	
)

(instance dealingScript of Script
	(properties)

	(method (changeState newState &tmp newcard)
		(switch (= state newState)
			(0
				(HandsOff)
				(= cycles 5)
			)
			(1
				(AddNewCard (GetNextCard) 1)
				(= cycles 5)
			)
			(2
				(AddNewCard (GetNextCard) 0)
				(= cycles 5)		
			)
			(3
				(AddNewCard (GetNextCard) 1)
				(= cycles 5)		
			)	
			(4
				(AddNewCard (GetNextCard) 0)
				(RecalcTotals)
				(CheckResult)
				(= gameState gStatePlayerTurn)	
				(HandsOn)
				(rm001 setScript: RoomScript)
				(dealingScript dispose:)
			)						
		)
	)
)

(instance addCardScript of Script


	(method (changeState newState &tmp newcard)
		(switch (= state newState)
			(0
				(HandsOff)
				(= cycles 1)
			)
			(1
				(AddNewCard (GetNextCard) 1)
				(= cycles 3)
			)
			(2
				(RecalcTotals)
				(CheckResult)					
				(HandsOn)
				(rm001 setScript: RoomScript)
				(addCardScript dispose:)				
			)
							
		)
	)
)	
(instance standScript of Script


	(method (changeState newState &tmp newcard)
		(switch (= state newState)
			(0
				(= gameState gStateCPUTurn)
				(HandsOff)
				(= cycles 1)
			)
			(1
				; Turn second card over 
				(hiddenDealerCard init:)
				(= cycles 3)
			)
			(2
				(if (< dealerTotal 17)
					(self changeState: 3)
				else
					(self changeState: 4)
				)
			)
			(3
				(AddNewCard (GetNextCard) 0)
				(= cycles 3)		
			)
			(4
				(RecalcTotals)
				(CheckResult)
			
				(if (and (>= dealerTotal 17) (<= dealerTotal 21))
					(CheckFinalResult)
				else
					(if (< dealerTotal 17)
						(self changeState: 3)
					)
				)
			)
							
		)
	)
)	


(instance dealButton of CView
	(properties
		y 20
		x 35
		view 6
		;loop 6
	)

	(method (handleEvent event)
		(if
			(and
				(not (event claimed:))
				(or
					(and (== (event type:) evKEYBOARD) (== (event message:) KEY_RETURN))
					(== (event type:) evMOUSEBUTTON)
				)
				(<= nsLeft (event x:) nsRight)
				(<= nsTop (event y:) nsBottom)
				;(<= nsTop (- (event y:) 10) nsBottom)
			)
			(event claimed: 1)
			(if (!= gameState gStateNotStarted)
				(Print 11 0)
			else
				(NewGame)
			)

		)
		(event claimed:)
	)
)

(instance hitButton of CView
	(properties
		y 20
		x 72
		view 6
		loop 0
		cel 1
	)

	(method (handleEvent event)
		(if
			(and
				(not (event claimed:))
				(or
					(and (== (event type:) evKEYBOARD) (== (event message:) KEY_RETURN))
					(== (event type:) evMOUSEBUTTON)
				)
				(<= nsLeft (event x:) nsRight)
				(<= nsTop (event y:) nsBottom)
				;(<= nsTop (- (event y:) 10) nsBottom)
			)
			(event claimed: 1)
				(if (!= gameState gStatePlayerTurn)
							(Print 11 12)
						else
							(rm001 setScript: addCardScript)
	  					)
		)
		(event claimed:)
	)
)

(instance standButton of CView
	(properties
		y 20
		x 109
		
		view 6
		loop 0
		cel 2
	)

	(method (handleEvent event)
		(if
			(and
				(not (event claimed:))
				(or
					(and (== (event type:) evKEYBOARD) (== (event message:) KEY_RETURN))
					(== (event type:) evMOUSEBUTTON)
				)
				(<= nsLeft (event x:) nsRight)
				(<= nsTop (event y:) nsBottom)
				;(<= nsTop (- (event y:) 10) nsBottom)
			)
			(event claimed: 1)

			(if (!= gameState gStatePlayerTurn)
				(Print 11 12)
			else
				(rm001 setScript: standScript)
			)

		)
		(event claimed:)
	)
)

(instance rm001 of Rm
	(properties
		picture scriptNumber
	)
	
	(method (init)
		(super init:)
		(dealButton init:)
		(hitButton init:)
		(standButton init:)
		(self setScript: RoomScript)
		(= gameState gStateNotStarted)
		(HandsOn)

	)
)

(instance RoomScript of Script
	(properties)
	(method (handleEvent pEvent &tmp mods)
		;(super handleEvent: event)
	
		(dealButton handleEvent: pEvent)
		(hitButton handleEvent: pEvent)
		(standButton handleEvent: pEvent)
		(switch (pEvent type:)
			;(evMOUSEBUTTON
			;	(= mods (pEvent modifiers:)) ; its the only way i could get it to work - i hate this
			;	(if (== mods 544)
			;		(Print {left click})
			;	)
			;)
			(evKEYBOARD
				(switch (pEvent message?)
	            	($3E00     ; F4 - start game
						(if (!= gameState gStateNotStarted)
							(Print 11 0)
						else
							(NewGame)
						)
	            	)
	            	($4000     ; F6 - hit
						(if (!= gameState gStatePlayerTurn)
							(Print 11 12)
						else
							(rm001 setScript: addCardScript)
	  					)
	            	)
	            	($4200     ; F8 - stand
						(if (!= gameState gStatePlayerTurn)
							(Print 11 12)
						else
							(rm001 setScript: standScript)
	  					)
	            	)
	        	)
			)
			(evSAID
				(if (Said 'change/bet') 
					(ChangeBet)
				)
			)
		)
		;(if (not (pEvent claimed:))
		;	(super handleEvent: pEvent)
		;)
	)
)
