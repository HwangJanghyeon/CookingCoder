package com.ccp5.controller;

import java.util.List;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.RestController;

import com.ccp5.dto.BoardDTO;
import com.ccp5.dto.IngrBoard;
import com.ccp5.repository.BoardRepository;
import com.ccp5.service.BoardService;
import com.ccp5.service.IngrListService;

@RestController
@RequestMapping("/api/boards")
public class BoardApiController {

	@Autowired
	BoardRepository boardRepository;
	@Autowired
	private BoardService boardService;
	@Autowired
	private IngrListService ilService;

	@GetMapping("/search")
	public ResponseEntity<List<BoardDTO>> searchBoards(@RequestParam String title) {
		// 검색 로직 구현
		List<BoardDTO> searchResults = boardService.searchByTitle(title);
		return ResponseEntity.ok(searchResults);
	}

	// 메인 화면에 게시글들 출력하는 데이터 모바일로 보내기
	@GetMapping
	public ResponseEntity<List<BoardDTO>> getAllBoards() {
		List<BoardDTO> boards = boardService.getAllBoards();
		return ResponseEntity.ok(boards);
	}

	// 모바일로 게시글 데이터 보내기
	@GetMapping("/{num}")
	public ResponseEntity<BoardDTO> getBoardByNum(@PathVariable int num) {
		BoardDTO board = boardService.getBoardByNum(num);
		if (board != null) {
			return ResponseEntity.ok(board);
		} else {
			return ResponseEntity.notFound().build();
		}
	}

	// 모바일로 재료 리스트 데이터 보내기
	@GetMapping("/{num}/ingredients")
	public ResponseEntity<List<IngrBoard>> getIngredientsForBoard(@PathVariable int num) {
		BoardDTO board = boardService.getBoardByNum(num);
		List<IngrBoard> ingrBoards = ilService.findByTitle(board.getTitle());
		if (ingrBoards != null && !ingrBoards.isEmpty()) {
			return ResponseEntity.ok(ingrBoards);
		} else {
			return ResponseEntity.notFound().build();
		}
	}

	// 모바일로 총 가격 데이터 보내기
	@GetMapping("/{num}/totalPrice")
	public ResponseEntity<Integer> getTotalPrice(@PathVariable int num) {
		Integer totalPrice = boardRepository.calculateTotalPriceByNum(num);
		if (totalPrice != null) {
			return ResponseEntity.ok(totalPrice);
		} else {
			return ResponseEntity.notFound().build();
		}
	}

	@PostMapping
	public ResponseEntity<Void> insertBoard(@RequestBody BoardDTO boardDTO) {
		boardService.insertBoard(boardDTO);
		return ResponseEntity.status(HttpStatus.CREATED).build();
	}

	@PostMapping("/updatePrice/{boardNum}")
	@ResponseBody
	public Integer updatePrice(@RequestBody Map<String, Object> requestBody, @PathVariable int boardNum) {
		// 요청으로부터 재료 이름과 버튼 텍스트를 가져옴
		String ingredientName = (String) requestBody.get("ingredientName");
		boolean isOwned = (boolean) requestBody.get("isOwned");
		System.out.println("ingredientName : " + ingredientName);
		System.out.println("isOwned : " + isOwned);

		// 버튼 텍스트가 '보유'인 경우에 해당하는 재료의 이름과 단위를 가져와 리포지토리의 쿼리에 전달하여 총 가격 계산
		if (!isOwned) {
			// 해당 재료의 총 가격을 계산하는 쿼리 실행
			Integer price = boardRepository.calculateTotalPriceByIngredientName(ingredientName, boardNum);
			System.out.println("totalprice : " + price);
			return price;
		}
		Integer price = boardRepository.calculateTotalPriceByIngredientName(ingredientName, boardNum) * -1;
		System.out.println(price);
		return price; // 보유 상태가 아닌 경우는 아직 처리하지 않음
	}
}
