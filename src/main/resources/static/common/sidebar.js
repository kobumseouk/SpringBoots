<div id="sidebar-placeholder"></div>

<script>
        document.addEventListener("DOMContentLoaded", function() {
        fetch("/common/sidebar.html")
            .then(response => {
                if (!response.ok) {
                    throw new Error("사이드바를 불러오는 중 오류가 발생했습니다.");
                }
                return response.text();
            })
            .then(data => {
                document.getElementById("sidebar-placeholder").innerHTML = data;
            })
            .catch(error => {
                console.error("사이드바를 로드할 수 없습니다:", error);
            });
    });
</script>
