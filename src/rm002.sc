;;; Sierra Script 1.0 - (do not remove this comment)
;
; SCI Template Game
; By Brian Provinciano
; ******************************************************************************
; rm002.sc
; Contains the first room of your game. 
(script# 2)
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
	rm002 0
)

(local
	gameState
	dealerText
	egoText	
	dealerTotal 
	egoTotal
)

(instance rm002 of Rm
	(properties
		picture scriptNumber
		; Set up the rooms to go to/come from here
		north 0
		east 0
		south 0
		west 0
	)
	
	(method (init)
		; same in every script, starts things up
		(super init:)
		(self setScript: RoomScript)
		(HandsOn)
		(= gameState gStateNotStarted)
		(Print 1 0 
			#title {How to play}
			#width 210)
		
	)
)


(instance RoomScript of Script
	(properties)
	
	(method (handleEvent pEvent &tmp temp0 temp1)
		(super handleEvent: pEvent)
		(if (== evKEYBOARD (pEvent type?))	
			(switch (pEvent message?)
            	($3E00     ; F4 - start game
					(NewGame)

            	)
            	($4000     ; F6 - hit
					(if (!= gameState gStatePlayerTurn)
						(Print 1 4)
					else
						(AddNewCard (GetNextCard) 1)
					)

            	)
            	($4200     ; F8 - stand
					(if (!= gameState gStatePlayerTurn)
						(Print 1 4)
					else
						(= gameState gStateCPUTurn)
						(rm002 setScript: dealingScript)
						(dealingScript changeState: 5)
					)

            	)
        	)

		)
		(if (Said 'change/bet') 
			
			(if (> gameState 0)
				(Print 1 3)
			
			else	
				(= temp1 0)
				
				(while (== temp1 0)
					(= temp0 (GetNumber {Enter new bet:}))
					
					(if (> temp0 gScore)
						(Print 1 1)	
					else
						(if (<= temp0 0)
							(Print 1 2)	
						else
							(= temp1 1)	
						)
					
					
						
					)
				)
				
				(= gCurrentBet temp0)
				(SL doit:)	
			)
		)
		
		(if (Said 'look')

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
				; pass execution to dealing animations
				;(dealingScript init:)
				(rm002 setScript: dealingScript)
				
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

(procedure (ResetGame &tmp i)
	;Re-init deck
	(deck release:)
	
	(for ((= i 0)) (< i 52) ((++ i))
		(deck addToEnd: i)
	)
)

(procedure (NewGame)
	(ResetGame)
	
	(-= gScore gCurrentBet)
	(= gameState gStateDealing)
	(shuffleAnim 
		setCycle: Fwd  
		setScript: shuffleScript
		init:)
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

(procedure (AddNewCard cardId isEgo &tmp newcard view loop cel newX cardCount cardValue)
	(= newcard (Clone card))
	(= view (/ cardId 13))   ; 0-12 = spades, 13-25 = clubs, etc.
    (= loop 0)
    (= cel (+ (mod cardId 13) 1))  ; 0-12 mod 13 + 1 = cels 1-13
    (= cardValue cel)
	(newcard 
		view: view
		loop: loop
		cel: cel)
		;x 60
	(if (== isEgo 1)
		(newcard y: 130)
		(= cardCount (egoCards size:))
	else
		(newcard y: 25)	
		(= cardCount (dealerCards size:))
		
		(if (>= cardCount 1)
			(newcard 
				view: 200
				loop: 0
				cel: 0)	
		)
	)
	(= newX (+ (* 40 cardCount) 35))
	
	;60 + (40 * cardcount)
	
	(newcard
		x: newX 
		init:)
		
	(if (== isEgo 1)
		(egoCards addToEnd: cardValue)
	else
		(dealerCards addToEnd: cardValue)
	)
	
	(RecalcTotals)
	
	(CheckResult)
	
)

(procedure (CheckResult)

	(if (== gameState gStatePlayerTurn)
		(if (> egoTotal 21)
			(EgoDead)
			(= gameState gStateNotStarted)
		)
	)
	(if (== gameState gStateCPUTurn)
		(if (> egoTotal 21)
			(Print {dealer busted - you win})
			(= gameState gStateNotStarted)
		)
		
	)
	
)

(procedure (RecalcTotals &tmp i node calc dealerAces egoAces [temp4 400])
	(Display 1 5 dsCOORD 10 15 dsRESTOREPIXELS dealerText)
	(Display 1 5 dsCOORD 10 115 dsRESTOREPIXELS egoText)
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
	(= dealerText (Display @temp4 dsCOORD 10 15 dsWIDTH 100 dsCOLOR 15 dsSAVEPIXELS))
	
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
	(= egoText (Display @temp4 dsCOORD 10 115 dsWIDTH 100 dsCOLOR 15 dsSAVEPIXELS))
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
				(= gameState gStatePlayerTurn)	
				(HandsOn)
				(rm002 setScript: RoomScript)
				(dealingScript dispose:)
			)		
			(5
			 
				
			)				
		)
	)
	
	
)

(procedure (EgoDead)
	(gGame setScript: (ScriptID DYING_SCRIPT)) ; DyingScript
	((ScriptID DYING_SCRIPT) ; DyingScript
		caller: (+ 4 (gEgo view:))
		register: {text 1} ; "Next time, %s, stay out of that polluted lagoon!"
		next:  {text 2} ; "You're in over your head again"
	)
	
)