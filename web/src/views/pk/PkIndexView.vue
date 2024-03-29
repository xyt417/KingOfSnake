<template>
    <div class="outer">
        <PlayGround v-if="$store.state.pk.status === 'playing'"/>
        <MatchGround v-if="$store.state.pk.status === 'matching'"/>
        <ResultBoard v-if="$store.state.pk.result !== 'none'"></ResultBoard>
    </div>
</template>

<script setup>
import PlayGround from "@/components/PlayGround.vue";
import MatchGround from "@/components/MatchGround.vue";
import {onMounted, onUnmounted} from "vue";
import {useStore} from "vuex";
import ResultBoard from "@/components/ResultBoard.vue";

const store = useStore();
const socketUrl = `ws://${localStorage.getItem('IpAddr')}:3000/websocket/${store.state.user.token}/`;

let socket = null;
onMounted(() => {
	// 设置匹配默认头像、对手昵称
	store.commit("updateOpponent", {
		username: "对手",
		photo: require("@/assets/images/1.png"), // 转换为打包后的路径
	});
	// 初始化游戏结果
	store.commit("updateResult", "none");

	// 创建一个新的 WebSocket 对象时，浏览器会自动尝试与提供的 URL 建立一个 WebSocket 连接，这个过程是异步的
    // 注: 由于后端根据 userId 来选择 WebSocket，所以关闭后重新连接，后端也会把信息发送到新的 WebSocket 上
	socket = new WebSocket(socketUrl);

	socket.onopen = () => {
		console.log("socket opened");
		store.commit("updateSocket", socket);
	}

	socket.onmessage = msg => {
		const data = JSON.parse(msg.data);
		console.log("game message:", data);
		if (data.event === "match-success" && store.state.pk.button_status === "matching") { // 匹配成功
			store.commit("updateOpponent", {
				username: data.opponent_username,
                photo: data.opponent_photo,
            })
			setTimeout(() => {
				store.commit("updateStatus", "playing"); // GameMap 会自动创建挂载
			}, 1000);
			store.commit("updateGame", data);
        } else if (data.event === "move" && store.state.pk.status === "playing") {
            const game = store.state.pk.gameObject;
			const [snake0, snake1] = game.snakes;
			snake0.setDirection(data.a_direction); // a -> snake0
			snake1.setDirection(data.b_direction); // b -> snake1
        } else if (data.event === "result" && store.state.pk.status === "playing") {
            const game = store.state.pk.gameObject;
			const [snake0, snake1] = game.snakes;
			if (data.result === "A" || data.result === "D") {
				snake0.status = "hit";
				snake0.setHitTimer();
            }
			if (data.result === "B" || data.result === "D") {
				snake1.status = "hit";
				snake1.setHitTimer();
			}
			store.commit("updateResult", data.result);
        }

	}

	socket.onclose = () => {
		console.log("socket closed");
	}

})

onUnmounted(() => {
	socket.close();
	store.state.pk.status = "matching";
})

</script>

<style scoped>
.outer {
    position: absolute;
    background-image: url('@/assets/images/Cyberpunk.png');
    background-position: center;
    background-size: cover;
    height: 100vh;
    width: 100vw;
}

</style>