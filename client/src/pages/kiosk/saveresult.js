import Footer from './footer';
import * as React from 'react';
import { useNavigate } from 'react-router-dom';
import Box from '@mui/material/Box';
import './saveresult.css'

function Savingresult() {
    let lockernum = 1 
    const navigate = useNavigate();
    const handleOpenSavePage = () => {
        // 메인 페이지로 이동
        navigate('/');
      };
    return (
        <div>
            {/* 로고 */}
            <img
            className="logo"
            src={process.env.PUBLIC_URL + '/img/kiosk/예비로고.png'}
            alt={'title'}
            ></img>
            <div className='saveresulttext1'>
                <p>{lockernum}번 사물함에 키 보관을 완료했습니다</p>
                <p>이용해주셔서 감사합니다.</p>
            </div>
            <Box component="span" className="saveresultbox1" onClick={handleOpenSavePage}>
                <p className="saveresulttext2">확인</p>
            </Box>
            <Footer />
        </div>
    )
}


export default Savingresult;