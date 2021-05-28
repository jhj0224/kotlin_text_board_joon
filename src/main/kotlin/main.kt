import java.text.SimpleDateFormat

var loginedMember : Member? = null

fun main() {
    println("== 프로그램 시작 ==")

    memberRepository.makeTestMember()
    boardRepository.makeTestBoards()
    articleRepository.makeTestArticle()

    val systemController = SystemController()
    val memberController = MemberController()
    val boardController = BoardController()
    val articleController = ArticleController()

    while (true) {
        val prompt = if (loginedMember == null) {
            "명령어) "
        } else {
            "${loginedMember!!.nickname}) "
        }
        print(prompt)
        val command = readLineTrim()
        val rq = Rq(command)

        when (rq.actionPath) {
            "/system/exit" -> {
                systemController.exit(rq)
                break
            }
            "/board/list" -> {
                boardController.list(rq)
            }
            "/board/make" -> {
                boardController.make(rq)
            }
            "/member/logout" -> {
                memberController.logout(rq)
            }
            "/member/login" -> {
                memberController.login(rq)
            }
            "/member/join" -> {
                memberController.join(rq)
            }
            "/article/write" -> {
                articleController.write(rq)
            }
            "/article/list" -> {
                articleController.list(rq)
            }
            "/article/detail" -> {
                articleController.detail(rq)
            }
            "/article/modify" -> {
                articleController.modify(rq)
            }
            "/article/delete" -> {
                articleController.delete(rq)
            }
        }

    }

    println("== 프로그램 종료 ==")
}



// Rq는 UserRequest의 줄임말이다.
// Request 라고 하지 않은 이유는, 이미 선점되어 있는 클래스명 이기 때문이다.
class Rq(command: String) {
    // 데이터 예시
    // 전체 URL : /artile/detail?id=1
    // actionPath : /artile/detail
    val actionPath: String

    // 데이터 예시
    // 전체 URL : /artile/detail?id=1&title=안녕
    // paramMap : {id:"1", title:"안녕"}
    private val paramMap: Map<String, String>

    // 객체 생성시 들어온 command 를 ?를 기준으로 나눈 후 추가 연산을 통해 actionPath와 paramMap의 초기화한다.
    // init은 객체 생성시 자동으로 딱 1번 실행된다.
    init {
        // ?를 기준으로 둘로 나눈다.
        val commandBits = command.split("?", limit = 2)

        // 앞부분은 actionPath
        actionPath = commandBits[0].trim()

        // 뒷부분이 있다면
        val queryStr = if (commandBits.lastIndex == 1 && commandBits[1].isNotEmpty()) {
            commandBits[1].trim()
        } else {
            ""
        }

        paramMap = if (queryStr.isEmpty()) {
            mapOf()
        } else {
            val paramMapTemp = mutableMapOf<String, String>()

            val queryStrBits = queryStr.split("&")

            for (queryStrBit in queryStrBits) {
                val queryStrBitBits = queryStrBit.split("=", limit = 2)
                val paramName = queryStrBitBits[0]
                val paramValue = if (queryStrBitBits.lastIndex == 1 && queryStrBitBits[1].isNotEmpty()) {
                    queryStrBitBits[1].trim()
                } else {
                    ""
                }

                if (paramValue.isNotEmpty()) {
                    paramMapTemp[paramName] = paramValue
                }
            }

            paramMapTemp.toMap()
        }
    }

    fun getStringParam(name: String, default: String): String {
        return paramMap[name] ?: default
    }

    fun getIntParam(name: String, default: Int): Int {
        return if (paramMap[name] != null) {
            try {
                paramMap[name]!!.toInt()
            } catch (e: NumberFormatException) {
                default
            }
        } else {
            default
        }
    }
}

class BoardController {
    fun list(rq: Rq) {
        println("번호 / 작성날짜 / 이름 / 코드")
        
        val boards = boardRepository.getfilteredBoards()

        for (board in boards) {
            println("${board.id} / ${board.regDate} / ${board.name} / ${board.code}")
        }
    }

    fun make(rq: Rq) {
        print("게시판 이름 : ")
        val name = readLineTrim()
        val boardByName = boardRepository.getBoardByName(name)

        if ( boardByName != null ) {
            println("`${name}`(은)는 이미 존재하는 게시판 이름 입니다.")

            return
        }

        print("게시판 코드 : ")
        val code = readLineTrim()
        val boardByCode = boardRepository.getBoardByCode(code)

        if ( boardByCode != null ) {
            println("`${code}`(은)는 이미 존재하는 게시판 코드 입니다.")

            return
        }

        val id = boardRepository.makeBoard(name, code)

        println("${id}번 게시판이 생성되었습니다.")
    }

}

class SystemController {
    fun exit(rq: Rq) {
        println("프로그램을 종료합니다.")
    }
}

class MemberController {
    fun logout(rq: Rq) {
        loginedMember = null
        println("로그아웃되었습니다.")
    }

    fun login(rq: Rq) {
        print("로그인 아이디 : ")
        val logId = readLineTrim()

        val member = memberRepository.getMemberBylogId(logId)

        if (member == null) {
            println("${logId}은(는) 존재하지 않는 로그인 아이디입니다.")
            return
        }

        print("로그인 비밀번호 : ")
        val logPw = readLineTrim()

        if (member.logPw != logPw) {
            println("비밀번호가 틀렸습니다.")
            return
        }
        loginedMember = member

        println("${member.nickname}님 환영합니다.")
    }

    fun join(rq: Rq) {
        print("로그인 아이디 : ")
        val logId = readLineTrim()

        val isjoinablelognied = memberRepository.isjoinablelognied(logId)

        if (isjoinablelognied == false) {
            println("${logId}은(는) 이미 사용중인 로그인 아이디입니다.")
            return
        }

        print("로그인 비밀번호 : ")
        val logPw = readLineTrim()
        print("이름 : ")
        val name = readLineTrim()
        print("별명 : ")
        val nickname = readLineTrim()
        print("휴대 전화번호 : ")
        val cellphoneno = readLineTrim()
        print("이메일 : ")
        val email = readLineTrim()

        val id = memberRepository.join(logId, logPw, name, nickname, cellphoneno, email)

        println("${id}번 회원가입을 축하드립니다.")
    }

}

class ArticleController {
    fun write(rq: Rq) {
        if (loginedMember == null) {
            println("로그인을 해주세요")
            return
        }

        print("게시판을 선택(공지사항 : 1 , 자유게시판 : 2)")
        val boardId = readLineTrim().toInt()

        print("제목 : ")
        val title = readLineTrim()
        print("내용 : ")
        val body = readLineTrim()

        val id = articleRepository.addArticle(boardId, loginedMember!!.id, title, body)

        println("${id}번 게시물이 추가되었습니다.")
    }

    fun list(rq: Rq) {
        val page = rq.getIntParam("page" , 1)
        val searchKeyword = rq.getStringParam("searchKeyword" , "")

        val filteredArticles = articleRepository.getfilteredArticles(searchKeyword, page, 10)

        println("번호 / 작성날짜 / 게시물종류 / 작성자 / 제목")
        for (article in filteredArticles) {
            val board = boardRepository.getBoardById(article.boardId)!!
            val boardname = board.name
            val writer = memberRepository.getMemberById(article.memberId)!!
            val writername = writer.nickname
            println("${article.id} / ${article.regDate} / ${boardname} / ${writername} / ${article.title}")
        }
    }

    fun detail(rq: Rq) {
        val id = rq.getIntParam("id" , 0)

        if (id == 0) {
            println("번호를 입력해주세요.")
            return
        }

        val article = articleRepository.getArticleById(id)

        if (article == null) {
            println("${id}번 게시물은 존재하지 않습니다.")
            return
        }

        if (article.memberId != loginedMember!!.id) {
            println("권한이 없습니다.")
            return
        }

        println("번호 : ${article.id}")
        println("작성날짜 : ${article.regDate}")
        println("갱신날짜 : ${article.updateDate}")
        println("제목 : ${article.title}")
        println("내용 : ${article.body}")
    }

    fun modify(rq: Rq) {
        if (loginedMember == null) {
            println("로그인을 해주세요")
            return
        }

        val id = rq.getIntParam("id" , 0)

        if (id == 0) {
            println("번호를 입력해주세요.")
            return
        }

        val article = articleRepository.getArticleById(id)

        if (article == null) {
            println("${id}번 게시물은 존재하지 않습니다.")
            return
        }

        if (article.memberId != loginedMember!!.id) {
            println("권한이 없습니다.")
            return
        }

        print("${id}번 게시물의 새 제목 : ")
        val title = readLineTrim()
        print("${id}번 게시물의 새 내용 : ")
        val body = readLineTrim()

        articleRepository.modifyArticle(id, title, body)

        println("${id}번 게시물이 수정되었습니다.")
    }

    fun delete(rq: Rq) {
        if (loginedMember == null) {
            println("로그인을 해주세요")
            return
        }

        val id = rq.getIntParam("id" , 0)

        if (id == 0) {
            println("번호를 입력해주세요.")
            return
        }

        val article = articleRepository.getArticleById(id)

        if (article == null) {
            println("${id}번 게시물은 존재하지 않습니다.")
            return
        }

        articleRepository.deleteArticle(article)

        println("${id}번 게시물이 삭제되었습니다.")
    }

}

data class Member(
    val id : Int,
    val regDate: String,
    val updateDate: String,
    val logId : String,
    val logPw : String,
    val name : String,
    val nickname : String,
    val cellphoneno : String,
    val email : String
)

class MemberRepository {
    val members = mutableListOf<Member>()
    var lastid = 0

    fun join(logId: String, logPw: String, name: String, nickname: String, cellphoneno: String, email: String): Int {
        val id = ++lastid
        val regDate = Util.getNowDateStr()
        val updateDate = Util.getNowDateStr()

        members.add(Member(id, regDate, updateDate, logId, logPw, name, nickname, cellphoneno, email))

        return id
    }

    fun isjoinablelognied(logId: String): Boolean {
        val member = getMemberBylogId(logId)

        return member == null
    }

    fun getMemberBylogId(logId: String): Member? {
        for (member in members) {
            if (member.logId == logId) {
                return member
            }
        }
        return null
    }

    fun makeTestMember() {
        for (id in 1..9) {
            join("user$id" , "user$id" , "이름$id" , "사용자$id" , "010-1234-123$id" , "user$id@test.com")
        }
    }

    fun getMemberById(id: Int): Member? {
        for (member in members) {
            if (member.id == id) {
                return member
            }
        }
        return null
    }


}
val memberRepository = MemberRepository()

data class Article(
    val id : Int,
    val regDate : String,
    var updateDate : String,
    val memberId : Int,
    val boardId : Int,
    var title : String,
    var body : String
)

class ArticleRepository {
    val articles = mutableListOf<Article>()
    var lastid = 0

    fun addArticle(memberId: Int, boardId : Int, title: String, body: String): Int {
        val id = ++lastid
        val regDate = Util.getNowDateStr()
        val updateDate = Util.getNowDateStr()

        articles.add(Article(id, regDate, updateDate, memberId, boardId, title, body))

        return id
    }

    fun makeTestArticle() {
        for (id in 1..25) {
            addArticle(id % 9 + 1 , id % 2 + 1 ,"제목_$id", "내용_$id")
        }
    }

    fun getArticleById(id: Int): Article? {
        for (article in articles) {
            if (article.id == id) {
                return article
            }
        }
        return null
    }

    fun modifyArticle(id : Int, title : String, body : String) {
        val article = getArticleById(id)!!

        article.title = title
        article.body = body
        article.updateDate = Util.getNowDateStr()
    }

    fun deleteArticle(article: Article) {
        articles.remove(article)
    }

    fun getfilteredArticles(searchKeyword: String, page: Int, itemsCountInAPage: Int): List<Article> {
        val filtered1Articles = getSearchKeywordfilteredArticles(articles, searchKeyword)
        val filtered2Articles = getPagefilteredArticles(filtered1Articles, page, itemsCountInAPage)

        return filtered2Articles
    }
    private fun getSearchKeywordfilteredArticles(articles: List<Article>, searchKeyword: String): List<Article> {
        val filteredArticles = mutableListOf<Article>()

        for (article in articles) {
            if (article.title.contains(searchKeyword)) {
                filteredArticles.add(article)
            }
        }
        return filteredArticles
    }
    private fun getPagefilteredArticles(articles: List<Article>, page: Int, itemsCountInAPage: Int): List<Article> {
        val filteredArticles = mutableListOf<Article>()

        val offsetCount = (page - 1) * itemsCountInAPage
        val startIndex = articles.lastIndex - offsetCount
        var endIndex = startIndex - (itemsCountInAPage - 1)

        if (endIndex < 0) {
            endIndex = 0
        }
        for (i in startIndex downTo endIndex) {
            filteredArticles.add(articles[i])
        }
        return filteredArticles
    }
}
val articleRepository = ArticleRepository()

data class Board(
    val id : Int,
    val regDate: String,
    val updateDate: String,
    val name : String,
    val code : String
)

class BoardRepository {
    private val boards = mutableListOf<Board>()
    var lastid = 0

    fun getfilteredBoards(): List<Board> {
        return boards
    }

    fun getBoardById(id: Int): Board? {
        for (board in boards) {
            if (board.id == id) {
                return board
            }
        }
        return null
    }

    fun getBoardByName(name: String): Board? {
        for (board in boards) {
            if (board.name == name) {
                return board
            }
        }
        return null
    }

    fun getBoardByCode(code: String): Board? {
        for (board in boards) {
            if (board.code == code) {
                return board
            }
        }
        return null
    }

    fun makeBoard(name: String, code: String): Int {
        val id = ++lastid
        val regDate = Util.getNowDateStr()
        val updateDate = Util.getNowDateStr()

        boards.add(Board(id, regDate, updateDate, name, code))

        return id
    }

    fun makeTestBoards() {
        makeBoard("공지사항" , "notice")
        makeBoard("자유게시판", "free")
    }

}

val boardRepository = BoardRepository()

// 유틸 관련 시작
fun readLineTrim() = readLine()!!.trim()

object Util {
    fun getNowDateStr(): String {
        val format = SimpleDateFormat("yyyy-MM-dd HH:mm:ss")

        return format.format(System.currentTimeMillis())
    }
}
// 유틸 관련 끝